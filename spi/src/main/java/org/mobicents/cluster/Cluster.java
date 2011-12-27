package org.mobicents.cluster;

import java.util.List;

import org.mobicents.cluster.data.ClusterDataSource;
import org.mobicents.cluster.listener.ClusterDataFailOverListener;
import org.mobicents.cluster.listener.ClusterDataRemovalListener;

/**
 * The base cluster interface, bound to a data source which wraps T.
 * 
 * @author martins
 * 
 * @param <T>
 */
public interface Cluster<T> {

	/**
	 * Adds the specified fail over listener.
	 * 
	 * @param listener
	 */
	public boolean addFailOverListener(ClusterDataFailOverListener listener);

	/**
	 * Removes the specified fail over listener.
	 * 
	 * @param listener
	 * @return
	 */
	public boolean removeFailOverListener(ClusterDataFailOverListener listener);

	/**
	 * Adds the specified data removal listener.
	 * 
	 * @param listener
	 */
	public boolean addDataRemovalListener(ClusterDataRemovalListener listener);

	/**
	 * Removes the specified data removal listener.
	 * 
	 * @param listener
	 * @return
	 */
	public boolean removeDataRemovalListener(ClusterDataRemovalListener listener);

	/**
	 * Retrieves the local address of the cluster node.
	 * 
	 * @return
	 */
	public ClusterNodeAddress getLocalAddress() throws IllegalStateException;

	/**
	 * Retrieves the members of the cluster.
	 * 
	 * @return
	 */
	public List<ClusterNodeAddress> getClusterMembers()
			throws IllegalStateException;

	/**
	 * Indicates if the cluster is in local mode.
	 * 
	 * @return
	 */
	public boolean isLocalMode();

	/**
	 * Indicates if the local node is the head of the cluster. If true it is
	 * safe to assume that we can perform cluster wide operations.
	 * 
	 * @return
	 */
	public boolean isHeadMember() throws IllegalStateException;

	/**
	 * Method to determine if this node is single node in the cluster.
	 * 
	 * @return <ul>
	 *         <li><b>true</b> - cluster mode is local || clusterMembers == 1
	 *         <li>
	 *         <li><b>false</b> - otherwise
	 *         <li>
	 *         </ul>
	 */
	public boolean isSingleMember() throws IllegalStateException;

	/**
	 * 
	 * @return the cluster data source
	 */
	public ClusterDataSource<T> getClusterDataSource();

	/**
	 * 
	 */
	public void startCluster() throws IllegalStateException;

	/**
	 * 
	 */
	public void stopCluster() throws IllegalStateException;

	/**
	 * 
	 * @return
	 */
	public boolean isStarted();

}
