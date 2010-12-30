package org.mobicents.cluster.data;

import org.mobicents.cluster.ClusterNodeAddress;

/**
 * Interface for clustered data.
 * 
 * @author martins
 * 
 */
public interface ClusterData {

	/**
	 * The key that identifies the data in cluster.
	 * 
	 * @return
	 */
	public ClusterDataKey getKey();

	/**
	 * Retrieves the related data object stored
	 * 
	 * @return
	 */
	public Object getDataObject();

	/**
	 * Retrieves the cluster node address that "owns" the data.
	 * 
	 * @return null if the owner is not set
	 */
	public ClusterNodeAddress getOwner();

	/**
	 * Sets the local cluster node as the data owner. This is needed for
	 * failover process and thus it should be invoked data creation that are
	 * selected by keys with the failover flag.
	 * 
	 * @throws UnsupportedOperationException
	 *             if the method is invoked on data which does not uses
	 *             failover.
	 */
	public void setOwner() throws UnsupportedOperationException;

	/**
	 * Stores a data object.
	 * 
	 * @param value
	 * @throws UnsupportedOperationException
	 *             if the related key does not indicates that data can be
	 *             stored.
	 */
	public void setDataObject(Object value)
			throws UnsupportedOperationException;

	/**
	 * Retrieves an array containing all references.
	 * 
	 * @return
	 */
	public ClusterDataKey[] getReferences();

	/**
	 * Adds a reference to the specified key.
	 * 
	 * @param reference
	 * @throws UnsupportedOperationException
	 *             if the related key does not indicates that references may be
	 *             used.
	 */
	public void addReference(ClusterDataKey reference)
			throws UnsupportedOperationException;

	/**
	 * Removes the reference to the specified key.
	 * 
	 * @param reference
	 * @return true if the reference was found and removed
	 */
	public boolean removeReference(ClusterDataKey reference);

	/**
	 * Indicates if exists a reference to the specified key.
	 * 
	 * @param reference
	 * @return
	 */
	public boolean containsReference(ClusterDataKey reference);

	/**
	 * Removes the cluster data
	 * 
	 * @param cascadeRemoval
	 *            if true removes all references too.
	 */
	public void remove(boolean cascadeRemoval);

}
