package org.mobicents.cluster;

/**
 * The key that identifies specific cluster data, and provides further info
 * regarding failover and serialization operations.
 * 
 * @author martins
 * 
 */
public interface ClusterDataKey {

	/**
	 * Indicates if key is used to store data.
	 * 
	 * @return
	 */
	public boolean storesData();

	/**
	 * Indicates if key is used as a references source.
	 * 
	 * @return
	 */
	public boolean usesReferences();

	/**
	 * Indicates if key must be failed over, that is, if in case its cluster
	 * member owner fails, another must be elected to own it and be warned.
	 * 
	 * @return
	 */
	public boolean isFailedOver();

	/**
	 * Retrieves the id for the marshaler that handles serialization of the key.
	 * 
	 * @return
	 */
	public int getMarshalerId();

	/**
	 * Retrieves the related listener key. Listener keys are used to match a key
	 * with a cluster listener, such as the ones handling fail over or remote
	 * data removal.
	 * 
	 * @return null if there is no defined listener key.
	 */
	public ClusterDataKey getListenerKey();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object arg0);

}
