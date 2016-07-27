/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.restcomm.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.infinispan.tree.Fqn;
import org.infinispan.tree.NodeKey;
import org.infinispan.tree.NodeKey.Type;
import org.infinispan.tree.TreeCache;
import org.restcomm.cache.MobicentsCache;
import org.restcomm.cluster.cache.ClusteredCacheData;
import org.restcomm.cluster.cache.ClusteredCacheDataIndexingHandler;
import org.restcomm.cluster.cache.DefaultClusteredCacheDataIndexingHandler;
import org.restcomm.cluster.election.ClientLocalListenerElector;
import org.restcomm.cluster.election.ClusterElector;


/**
 * Listener that is to be used for cluster wide replication(meaning no buddy
 * replication, no data gravitation). It will index activity on nodes marking
 * current node as owner(this is semi-gravitation behavior (we don't delete, we
 * just mark)). 
 * 
 * Indexing is only at node level, i.e., there is no
 * reverse indexing, so it has to iterate through whole resource group data FQNs to check which
 * nodes should be taken over.
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * @author martins
 * @author András Kőkuti
 */

@Listener
public class DefaultMobicentsCluster implements MobicentsCluster {


	private static final Logger logger = Logger.getLogger(DefaultMobicentsCluster.class);

	private final SortedSet<FailOverListener> failOverListeners;
	@SuppressWarnings("unchecked")
	private final ConcurrentHashMap<Fqn, DataRemovalListener> dataRemovalListeners;
	
	private final MobicentsCache mobicentsCache;
	private final TransactionManager txMgr;
	private final ClusterElector elector;
	private final DefaultClusteredCacheDataIndexingHandler clusteredCacheDataIndexingHandler;
	
	private List<Address> currentView;
	
	private boolean started;
	
	@SuppressWarnings("unchecked")
	public DefaultMobicentsCluster(MobicentsCache watchedCache, TransactionManager txMgr, ClusterElector elector) {
		this.failOverListeners = Collections.synchronizedSortedSet(new TreeSet<FailOverListener>(new FailOverListenerPriorityComparator()));
		this.dataRemovalListeners = new ConcurrentHashMap<Fqn, DataRemovalListener>();
		this.mobicentsCache = watchedCache;
		this.txMgr = txMgr;
		this.elector = elector;
		this.clusteredCacheDataIndexingHandler = new DefaultClusteredCacheDataIndexingHandler();
	}

	/* (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#getLocalAddress()
	 */
	public Address getLocalAddress() {		
		return mobicentsCache.getJBossCache().getCache().getCacheManager().getAddress();
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#getClusterMembers()
	 */
	public List<Address> getClusterMembers() {
		if (currentView != null) {
			return Collections.unmodifiableList(currentView);
		}
		else {
			final Address localAddress = getLocalAddress();
			if (localAddress == null) {
				return Collections.emptyList();
			}
			else {
				final List<Address> list = new ArrayList<Address>();
				list.add(localAddress);
				return Collections.unmodifiableList(list);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#isHeadMember()
	 */
	public boolean isHeadMember() {
		final Address localAddress = getLocalAddress();
		if (localAddress != null) {
			final List<Address> clusterMembers = getClusterMembers();
			return !clusterMembers.isEmpty() && clusterMembers.get(0).equals(localAddress);
		}
		else {
			return true;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#isSingleMember()
	 */
	public boolean isSingleMember() {
		final Address localAddress = getLocalAddress();
		if (localAddress != null) {
			final List<Address> clusterMembers = getClusterMembers();
			return clusterMembers.size() == 1;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Method handle a change on the cluster members set
	 * @param event
	 */
	@ViewChanged
	public synchronized void viewChanged(ViewChangedEvent event) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("onViewChangeEvent : id[" + event.getViewId() + "] : event local address[" + event.getLocalAddress() + "]");
		}
		
		final List<Address> oldView = currentView;
		currentView = new ArrayList<Address>(event.getNewMembers());
		final Address localAddress = getLocalAddress();
		
		//just a precaution, it can be null!
		if (oldView != null) {
			
			// recover stuff from lost members
			Runnable runnable = new Runnable() {
				public void run() {
					for (Address oldMember : oldView) {
						if (!currentView.contains(oldMember)) {
							if (logger.isDebugEnabled()) {
								logger.debug("onViewChangeEvent : processing lost member " + oldMember);
							}
							for (FailOverListener localListener : failOverListeners) {
								ClientLocalListenerElector localListenerElector = localListener.getElector();
								
								if (localListenerElector != null) {
									// going to use the local listener elector instead, which gives results based on data
									performTakeOver(localListener,oldMember,localAddress, true);
								}
								else {
									
									List<Address> electionView = getElectionView(oldMember);
									if(electionView!=null && elector.elect(electionView).equals(localAddress))
									{
										performTakeOver(localListener, oldMember, localAddress, false);
									}
									
									//cleanAfterTakeOver(localListener, oldMember);
								}
							}
						}
					}
				}
			};
			Thread t = new Thread(runnable);
			t.start();
		}
		
	}
	
	
	
	@SuppressWarnings("rawtypes")
	@CacheEntryRemoved
	public void cacheEntryRemoved(CacheEntryRemovedEvent event){
		if (logger.isDebugEnabled()) {
			logger.debug("cacheEntryRemoved : event[ "+ event +"]");
		}
		if(!event.isPre() && !event.isOriginLocal() && event.getKey() != null && (event.getKey() instanceof NodeKey)  && ((NodeKey)event.getKey()).getContents() == Type.STRUCTURE){
			
			Fqn changed = ((NodeKey)event.getKey()).getFqn();
			
			final DataRemovalListener dataRemovalListener = dataRemovalListeners.get(changed.getParent());
			if (dataRemovalListener != null) {
				dataRemovalListener.dataRemoved(changed);
			}
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void performTakeOver(FailOverListener localListener, Address lostMember, Address localAddress, boolean useLocalListenerElector) {
		//WARNING1: avoid using string representation, it may look ok, but hash is different if Fqn is not composed only from strings
		//WARNING2: use Fqn.fromRelativeElemenets(); -- Fqn.fromElements(); adds Fqn.SEPARATOR at beggin of Fqn.
		if (logger.isDebugEnabled()) {
			logger.debug("onViewChangeEvent : " + localAddress + " failing over lost member " + lostMember + ", useLocalListenerElector=" + useLocalListenerElector);
		}
			final TreeCache jbossCache = mobicentsCache.getJBossCache();
			final Fqn rootFqnOfChanges = localListener.getBaseFqn();
			//final String rootCacheOfChanges = localListener.getCacheName();
			
			boolean createdTx = false;
			boolean doRollback = true;
			
			try {
				if (txMgr != null && txMgr.getTransaction() == null) {
					txMgr.begin();
					createdTx = true;
				}
				
				
				if (createdTx) {
					txMgr.commit();
					createdTx = false;
				}
				
				if (txMgr != null && txMgr.getTransaction() == null) {
					txMgr.begin();
					createdTx = true;
				}
											
				localListener.failOverClusterMember(lostMember);
				Set<Object> children = jbossCache.getNode(rootFqnOfChanges).getChildrenNames();
				for (Object childName : children) {
					// Here in values we store data and... inet node., we must match
					// passed one.
					final ClusteredCacheData clusteredCacheData = new ClusteredCacheData(Fqn.fromRelativeElements(rootFqnOfChanges, childName),this);
					if (clusteredCacheData.exists()) {
						Address address = clusteredCacheData.getClusterNodeAddress();
						if (address != null && address.equals(lostMember)) {
							// may need to do election using client local listener
							
							if (useLocalListenerElector) {
								if(!localAddress.equals(localListener.getElector().elect(currentView, clusteredCacheData))) {
									// not elected, move on
									continue;
								}
							}
							// call back the listener
							localListener.wonOwnership(clusteredCacheData);
							// change ownership
							clusteredCacheData.setClusterNodeAddress(localAddress);							
						}					
					}else
					{
						//FIXME: debug?
						if(logger.isDebugEnabled())
						{
							logger.debug(" Attempt to index: "+Fqn.fromRelativeElements(rootFqnOfChanges, childName)+" failed, node does not exist.");
						}
					}
				}
				doRollback = false;
				
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				
			} finally {
				if (createdTx) {					
					try {
						if (!doRollback) {
							txMgr.commit();
						}
						else {
							txMgr.rollback();
						}
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}
				}
			}

	}

	

	private List<Address> getElectionView(Address deadMember) {
		
		return currentView;
	
	}

	
	/*private String getBuddyBackupFqn(Address owner)
	{
		//FIXME: switch to BuddyFqnTransformer
		String lostMemberFqnizied = owner.toString().replace(":", "_");
		String fqn = BUDDY_BACKUP_FQN_ROOT + lostMemberFqnizied ;				
		return fqn;
	}*/
	

	// NOTE USED FOR NOW
	
	/*
	@NodeCreated
	public void onNodeCreateddEvent(NodeCreatedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onNodeCreateddEvent : " + event.getFqn() + " : local[" + event.isOriginLocal() + "] pre[" + event.isPre() + "] : event local address[" + event.getCache().getLocalAddress()
					+ "]");
		}
	}

	@NodeModified
	public void onNodeModifiedEvent(NodeModifiedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onNodeModifiedEvent : pre[" + event.isPre() + "] : event local address[" + event.getCache().getLocalAddress() + "]");
		}
	}

	@NodeMoved
	public void onNodeMovedEvent(NodeMovedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onNodeMovedEvent : " + event.getFqn() + " : local[" + event.isOriginLocal() + "] pre[" + event.isPre() + "] ");
		}
	}

	@NodeVisited
	public void onNodeVisitedEvent(NodeVisitedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onNodeVisitedEvent : " + event.getFqn() + " : local[" + event.isOriginLocal() + "] pre[" + event.isPre() + "] ");
		}
	}

	@NodeLoaded
	public void onNodeLoadedEvent(NodeLoadedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onNodeLoadedEvent : " + event.getFqn() + " : local[" + event.isOriginLocal() + "] pre[" + event.isPre() + "] ");
		}
	}

	@NodeEvicted
	public void onNodeEvictedEvent(NodeEvictedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onNodeEvictedEvent : " + event.getFqn() + " : local[" + event.isOriginLocal() + "] pre[" + event.isPre() + "] ");
		}
	}

	@NodeInvalidated
	public void onNodeInvalidatedEvent(NodeInvalidatedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onNodeInvalidatedEvent : " + event.getFqn() + " : local[" + event.isOriginLocal() + "] pre[" + event.isPre() + "] ");
		}
	}

	@NodeActivated
	public void onNodeActivatedEvent(NodeActivatedEvent event) {

		if (log.isDebugEnabled()) {
			log.debug("onNodeActivatedEvent : " + event.getFqn() + " : local[" + event.isOriginLocal() + "] pre[" + event.isPre() + "] ");
		}
	}

	@NodePassivated
	public void onNodePassivatedEvent(NodePassivatedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onNodePassivatedEvent : " + event.getFqn() + " : local[" + event.isOriginLocal() + "] pre[" + event.isPre() + "] ");
		}
	}

	@BuddyGroupChanged
	public void onBuddyGroupChangedEvent(BuddyGroupChangedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onBuddyGroupChangedEvent : pre[" + event.isPre() + "] ");
		}
	}

	@CacheStarted
	public void onCacheStartedEvent(CacheStartedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onCacheStartedEvent : pre[" + event.isPre() + "] ");
		}
	}

	@CacheStopped
	public void onCacheStoppedEvent(CacheStoppedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("onCacheStoppedEvent : pre[" + event.isPre() + "] ");
		}
	}
	*/

	// LOCAL LISTENERS MANAGEMENT
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#addFailOverListener(org.mobicents.cluster.FailOverListener)
	 */
	public boolean addFailOverListener(FailOverListener localListener) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding local listener " + localListener);
		}
		for(FailOverListener failOverListener : failOverListeners) {
			if (failOverListener.getBaseFqn().equals(localListener.getBaseFqn())) {
				return false; 
			}
		}
		return failOverListeners.add(localListener);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#removeFailOverListener(org.mobicents.cluster.FailOverListener)
	 */
	public boolean removeFailOverListener(FailOverListener localListener) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing local listener " + localListener);
		}
		return failOverListeners.remove(localListener);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#addDataRemovalListener(org.mobicents.cluster.DataRemovalListener)
	 */
	public boolean addDataRemovalListener(DataRemovalListener listener) {
		return dataRemovalListeners.putIfAbsent(listener.getBaseFqn(), listener) == null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#removeDataRemovalListener(org.mobicents.cluster.DataRemovalListener)
	 */
	public boolean removeDataRemovalListener(DataRemovalListener listener) {
		return dataRemovalListeners.remove(listener.getBaseFqn()) != null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#getMobicentsCache()
	 */
	public MobicentsCache getMobicentsCache() {
		return mobicentsCache;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.cluster.MobicentsCluster#getClusteredCacheDataIndexingHandler()
	 */
	public ClusteredCacheDataIndexingHandler getClusteredCacheDataIndexingHandler() {
		return clusteredCacheDataIndexingHandler;
	}
	
	@Override
	public void startCluster() {
		synchronized (this) {
			if (started) {
				throw new IllegalStateException("cluster already started");
			}
			mobicentsCache.startCache();
			final TreeCache cache = mobicentsCache.getJBossCache();
			if (!cache.getCache().getCacheConfiguration().clustering().cacheMode().equals(CacheMode.LOCAL)) {
				
				logger.info("registring listener!");
				
				// get current cluster members
				currentView = new ArrayList<Address>(cache.getCache().getCacheManager().getMembers());
				
				
				// start listening to cache events
				cache.getCache().addListener(this);
				// start listening to cache manager events
				cache.getCache().getCacheManager().addListener(this);				
						
			}
			started = true;
		}				
	}
	
	@Override
	public boolean isStarted() {
		synchronized (this) {
			return started;
		}
	}
	
	@Override
	public void stopCluster() {
		synchronized (this) {
			if (!started) {
				throw new IllegalStateException("cluster already started");
			}
			mobicentsCache.stopCache();
			started = false;
		}				
	}
	
	
	
}
