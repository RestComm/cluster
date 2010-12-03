package org.mobicents.cluster;

/**
 * The data source for cluster data, wraps T.
 * 
 * @author martins
 *
 * @param <T>
 */
public interface ClusterDataSource<T> {

	/**
	 * The wrapped data source.
	 * @return
	 */
	public T getWrappedDataSource();
	
	/**
	 * Retrieves the cluster data interface for the specified key.
	 * @param key
	 * @return
	 */
	public ClusterData getClusterData(ClusterDataKey key);
	
	/**
	 * Indicates if the data source is in local or cluster mode.
	 * @return
	 */
	public boolean isLocalMode();
	
}
