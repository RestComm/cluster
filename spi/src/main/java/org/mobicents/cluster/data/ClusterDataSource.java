package org.mobicents.cluster.data;

import org.mobicents.cluster.data.marshall.ClusterDataMarshaller;

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
	 * Indicates if the data source is in local or cluster mode.
	 * 
	 * @return
	 */
	public boolean isLocalMode();

	/**
	 * Indicates if the data source is initiated
	 * @return
	 */
	public boolean isStarted();
	
	/**
	 * 
	 */
	public void startDatasource();
	
	/**
	 * 
	 */
	public void stopDatasource();
	
	/**
	 * Adds a Marshaller.
	 * @param <S>
	 * @param marshaller
	 * @throws IllegalStateException if the datasource is started
	 */
	public <S> void addMarshaller(ClusterDataMarshaller<S> marshaller) throws IllegalStateException;

}
