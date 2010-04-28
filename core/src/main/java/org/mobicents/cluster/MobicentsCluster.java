package org.mobicents.cluster;

import java.util.List;

import org.jgroups.Address;
import org.mobicents.cache.MobicentsCache;
import org.mobicents.cluster.cache.ClusteredCacheData;
import org.mobicents.cluster.cache.ClusteredCacheDataIndexingHandler;

/**
 * 
 * @author martins
 *
 */
public interface MobicentsCluster {

	/**
	 * Adds the specified fail over listener.
	 * @param listener
	 */
	public boolean addFailOverListener(FailOverListener listener);
	
	/**
	 * Removes the specified fail over listener.
	 * @param listener
	 * @return
	 */
	public boolean removeFailOverListener(FailOverListener listener);
	
	/**
	 * Adds the specified data removal listener.
	 * @param listener
	 */
	public boolean addDataRemovalListener(DataRemovalListener listener);
	
	/**
	 * Removes the specified data removal listener.
	 * @param listener
	 * @return
	 */
	public boolean removeDataRemovalListener(DataRemovalListener listener);
	
	/**
	 * Retrieves the local address of the cluster node.
	 * @return
	 */
	public Address getLocalAddress();
	
	/**
	 * Retrieves the members of the cluster.
	 * @return
	 */
	public List<Address> getClusterMembers();
	
	/**
	 * Indicates if the local node is the head of the cluster. If true it is safe to asume that we can perform cluster wide operations.
	 * @return
	 */
	public boolean isHeadMember();
	
	/**
	 * Method to determine if this node is single node in the cluster.
	 * 
	 * @return <ul>
	 *         <li><b>true</b> - cache mode is local || clusterMembers == 1
	 *         <li>
	 *         <li><b>false</b> - otherwise
	 *         <li>
	 *         </ul>
	 */
	public boolean isSingleMember();
	
	/**
	 *  
	 * @return the mobicents cache controlled by the cluster
	 */
	public MobicentsCache getMobicentsCache();
	
	/**
	 * Retrieves the handler to manage cluster indexing of {@link ClusteredCacheData}
	 * @return
	 */
	public ClusteredCacheDataIndexingHandler getClusteredCacheDataIndexingHandler();
}
