package org.mobicents.cluster.data;

import org.mobicents.cluster.Cluster;

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

	/**
	 * Initiates the data source. This should only be invoked when using the
	 * data source without a {@link Cluster}.
	 */
	public void init();

	/**
	 * Shuts down the data source. This should only be invoked when using the
	 * data source without a {@link Cluster}.
	 */
	public void shutdown();

}
