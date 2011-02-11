package org.mobicents.cluster.data;

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
	 * 
	 * @return
	 */
	public T getWrappedDataSource();

	/**
	 * Retrieves the cluster data interface for the specified key.
	 * 
	 * @param key
	 * @return
	 */
	public ClusterData getClusterData(ClusterDataKey key);

}
