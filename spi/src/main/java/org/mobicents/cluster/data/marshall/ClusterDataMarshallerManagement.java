package org.mobicents.cluster.data.marshall;

/**
 * Manages marshallers.
 * 
 * @author martins
 *
 */
public interface ClusterDataMarshallerManagement {

	/**
	 * Registers a marshaller with the specified id.
	 * @param marshallerId
	 * @param marshaller
	 */
	public void register(int marshallerId, ClusterDataMarshaller marshaller);
	
	/**
	 * Unregisters the marshaller with the specified id.
	 * @param marshallerId
	 */
	public void unregister(int marshallerId);
	
	/**
	 * Retrieves the marshaller with the specified id.
	 * @param marshallerId
	 * @return
	 */
	public ClusterDataMarshaller get(int marshallerId);
	
}
