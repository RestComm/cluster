package org.mobicents.cluster.hazelcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.mobicents.cluster.ClusterNodeAddress;
import org.mobicents.cluster.base.AbstractCluster;
import org.mobicents.cluster.data.ClusterData;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.ClusterDataSource;
import org.mobicents.cluster.elector.LocalFailoverElector;
import org.mobicents.cluster.hazelcast.data.HazelcastClusterDataSource;
import org.mobicents.cluster.hazelcast.elector.HazelcastFailOverElector;
import org.mobicents.cluster.listener.ClusterDataFailOverListener;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class HazelcastCluster extends AbstractCluster<HazelcastInstance>
		implements MembershipListener {

	private static final Logger LOGGER = Logger
			.getLogger(HazelcastCluster.class);

	/**
	 * failover elector
	 */
	private final HazelcastFailOverElector failOverElector = new HazelcastFailOverElector();

	/**
	 * address of the local cluster node
	 */
	private HazelcastClusterNodeAddress localAddress;

	/**
	 * the JTA tx manager
	 */
	private final TransactionManager txManager;

	/**
	 * the current list of cluster nodes
	 */
	private List<ClusterNodeAddress> clusterNodes;

	/**
	 * the datasource
	 */
	private final HazelcastClusterDataSource clusterDataSource;

	private final Cluster cluster = Hazelcast.getCluster();

	public HazelcastCluster(TransactionManager txManager) {
		super();
		this.txManager = txManager;
		this.clusterDataSource = new HazelcastClusterDataSource(txManager);
	}

	@Override
	public boolean isLocalMode() {
		return false;
	}

	@Override
	public ClusterDataSource<HazelcastInstance> getClusterDataSource() {
		return clusterDataSource;
	}

	private List<ClusterNodeAddress> createClusterMembersList(Set<Member> set) {
		final List<ClusterNodeAddress> result = new ArrayList<ClusterNodeAddress>();
		for (Member member : set) {
			result.add(new HazelcastClusterNodeAddress().setAddress(member.getInetSocketAddress().toString()));
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public void startCluster() throws IllegalStateException {
		synchronized (this) {
			throwExceptionIfClusterStarted();
			clusterDataSource.init();
			localAddress = new HazelcastClusterNodeAddress().setAddress(cluster
					.getLocalMember().getInetSocketAddress().toString());
			clusterNodes = createClusterMembersList(cluster.getMembers());
			cluster.addMembershipListener(this);
			started = true;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Mobicents Hazelcast Cluster started.");
			}
		}
	}

	@Override
	public void stopCluster() throws IllegalStateException {
		synchronized (this) {
			throwExceptionIfClusterNotStarted();
			clusterDataSource.shutdown();
			started = false;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Mobicents Hazelcast Cluster stopped.");
			}
		}
	}

	@Override
	public ClusterNodeAddress getLocalAddress() {
		throwExceptionIfClusterNotStarted();
		return localAddress;
	}

	@Override
	public List<ClusterNodeAddress> getClusterMembers() {
		throwExceptionIfClusterNotStarted();
		return clusterNodes;
	}

	@Override
	public void memberAdded(MembershipEvent event) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("memberAdded: member = " + event.getMember());
		}
		clusterNodes = createClusterMembersList(cluster.getMembers());
	}

	@Override
	public void memberRemoved(final MembershipEvent event) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("memberRemoved: member = " + event.getMember());
		}
		clusterNodes = createClusterMembersList(cluster.getMembers());

		// recover stuff from lost member
		Runnable runnable = new Runnable() {
			public void run() {
				ClusterNodeAddress oldNode = new HazelcastClusterNodeAddress()
						.setAddress(event.getMember().getInetSocketAddress().toString());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Starting failover of lost node " + oldNode);
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
							performTakeOver(localListener, oldNode, false);
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

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Performing take over of lost node " + lostNode
					+ ", for cluster data keys referenced by "
					+ localListener.getDataFailoverListenerKey());
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
					.getClusterData(localListener.getDataFailoverListenerKey());
			ClusterData clusterData = null;
			ClusterNodeAddress clusterDataOwner = null;
			for (ClusterDataKey key : baseClusterData.getReferences()) {
				// get the referenced key data
				clusterData = clusterDataSource
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
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Referenced key "
								+ key
								+ " now owned by local node, after failover of node "
								+ lostNode);
					}
					// call back the listener
					localListener.failover(clusterData);
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
