package org.mobicents.cluster.infinispan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.mobicents.cluster.ClusterData;
import org.mobicents.cluster.ClusterDataFailOverListener;
import org.mobicents.cluster.ClusterDataKey;
import org.mobicents.cluster.ClusterDataRemovalListener;
import org.mobicents.cluster.ClusterNodeAddress;
import org.mobicents.cluster.LocalFailoverElector;
import org.mobicents.cluster.base.AbstractCluster;

/**
 * Infinispan impl for Mobicents Cluster.
 * 
 * @author martins
 * 
 */
@SuppressWarnings("rawtypes")
@Listener(sync = false)
public class InfinispanCluster extends AbstractCluster<Cache> {

	private static final Logger LOGGER = Logger
			.getLogger(InfinispanCluster.class);

	/**
	 * failover elector
	 */
	private final InfinispanFailOverElector failOverElector = new InfinispanFailOverElector();

	/**
	 * address of the local cluster node
	 */
	private final InfinispanClusterNodeAddress localAddress;

	/**
	 * the JTA tx manager
	 */
	private final TransactionManager txManager;

	/**
	 * the current list of cluster nodes
	 */
	private List<ClusterNodeAddress> clusterNodes;

	/**
	 * 
	 * @param dataSource
	 * @param txMgr
	 */
	public InfinispanCluster(InfinispanClusterDataSource dataSource,
			TransactionManager txMgr) {
		super(dataSource);
		if (!clusterDataSource.isLocalMode()) {
			// cluster mode
			localAddress = new InfinispanClusterNodeAddress(clusterDataSource
					.getWrappedDataSource().getAdvancedCache().getRpcManager()
					.getTransport().getAddress());
			// create initial members list
			clusterNodes = createClusterMembersList(clusterDataSource
					.getWrappedDataSource().getAdvancedCache().getRpcManager()
					.getTransport().getMembers());
			// connect to infinispan as listener
			clusterDataSource.getWrappedDataSource().addListener(this);
		} else {
			// local mode
			localAddress = null;
			clusterNodes = Collections.emptyList();
		}
		this.txManager = txMgr;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Mobicents Infinispan Cluster started.");
		}
	}

	private List<ClusterNodeAddress> createClusterMembersList(
			List<Address> addressList) {
		final List<ClusterNodeAddress> result = new ArrayList<ClusterNodeAddress>();
		for (Address address : addressList) {
			result.add(new InfinispanClusterNodeAddress(address));
		}
		return Collections.unmodifiableList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.base.AbstractCluster#getLocalAddress()
	 */
	@Override
	public ClusterNodeAddress getLocalAddress() {
		return localAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.base.AbstractCluster#getClusterMembers()
	 */
	@Override
	public List<ClusterNodeAddress> getClusterMembers() {
		return clusterNodes;
	}

	/**
	 * Event handler of Infinispan cache removals.
	 * 
	 * @param event
	 */
	@CacheEntryRemoved
	public void onCacheEntryRemovedEvent(CacheEntryRemovedEvent event) {
		if (!event.isOriginLocal() && !event.isPre()) {
			final ClusterDataKey dataKey = event.getKey() instanceof ClusterDataKey ? ((ClusterDataKey) event
					.getKey()) : null;
			if (dataKey == null) {
				return;
			}
			final ClusterDataKey listenerKey = dataKey.getListenerKey();
			if (listenerKey == null) {
				return;
			}
			final ClusterDataRemovalListener dataRemovalListener = dataRemovalListeners
					.get(listenerKey);
			if (dataRemovalListener != null) {
				dataRemovalListener.dataRemoved(dataKey);
			}
		}
	}

	/**
	 * Event handler of Infinispan cluster node list changes.
	 * 
	 * @param event
	 */
	@ViewChanged
	public synchronized void onViewChangeEvent(ViewChangedEvent event) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Cluster view changed. Old nodes = "
					+ event.getOldMembers() + ", new nodes = "
					+ event.getNewMembers());
		}

		final List<ClusterNodeAddress> oldClusterNodes = clusterNodes;
		clusterNodes = createClusterMembersList(event.getNewMembers());

		// recover stuff from lost members
		Runnable runnable = new Runnable() {
			public void run() {
				for (ClusterNodeAddress oldNode : oldClusterNodes) {
					if (!clusterNodes.contains(oldNode)) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Starting failover of lost node "
									+ oldNode);
						}
						for (ClusterDataFailOverListener localListener : failOverListeners) {
							LocalFailoverElector localFailoverElector = localListener
									.getLocalElector();
							if (localFailoverElector != null) {
								// going to use the local failover elector
								// instead, which gives results based on data
								performTakeOver(localListener, oldNode, true);
							} else {
								if (failOverElector.elect(clusterNodes).equals(
										localAddress)) {
									performTakeOver(localListener, oldNode,
											false);
								}
							}
						}
					}
				}
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}

	/**
     * 
     */
	private void performTakeOver(ClusterDataFailOverListener localListener,
			ClusterNodeAddress lostNode, boolean useLocalListenerElector) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Performing take over of lost node " + lostNode);
		}

		boolean createdTx = false;
		boolean doRollback = true;

		try {
			if (txManager != null && txManager.getTransaction() == null) {
				txManager.begin();
				createdTx = true;
			}
			// warn listener about failover
			localListener.failOverClusterMember(lostNode);
			// get base cluster data from listener, we will get all it's
			// references
			ClusterData baseClusterData = clusterDataSource
					.getClusterData(localListener.getListenerKey());
			ClusterData clusterData = null;
			ClusterNodeAddress clusterDataOwner = null;
			for (ClusterDataKey key : baseClusterData.getReferences()) {
				// get the referenced key data
				clusterData = (InfinispanClusterData) clusterDataSource
						.getClusterData(key);
				// get the data owner
				clusterDataOwner = clusterData.getOwner();
				if (clusterDataOwner != null
						&& clusterDataOwner.equals(lostNode)) {
					// may need to do election using client local listener
					if (useLocalListenerElector) {
						if (!localAddress.equals(localListener
								.getLocalElector().elect(clusterNodes,
										clusterData))) {
							// not elected, move on
							continue;
						}
					}
					// call back the listener
					localListener.wonOwnership(clusterData);
					// change ownership
					clusterData.setOwner();
				}
			}
			doRollback = false;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (createdTx) {
				try {
					if (!doRollback) {
						txManager.commit();
					} else {
						txManager.rollback();
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

}
