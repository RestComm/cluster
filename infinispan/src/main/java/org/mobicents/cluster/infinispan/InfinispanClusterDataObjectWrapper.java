package org.mobicents.cluster.infinispan;

import org.infinispan.marshall.Marshallable;
import org.mobicents.cluster.infinispan.marshal.InfinispanClusterDataObjectWrapperExternalizer;

/**
 * Wrapper for cluster data object, which has an Externalizer in Infinispan to
 * handle serialization. The wrapper is needed to add own Mobicents Cluster
 * marshaller id.
 * 
 * @author martins
 * 
 */
@Marshallable(id = InfinispanClusterDataObjectWrapperExternalizer.ID)
public class InfinispanClusterDataObjectWrapper {

	private final Object dataObject;
	private final int marshallerId;

	/**
	 * 
	 * @param dataObject
	 * @param marshallerId
	 */
	public InfinispanClusterDataObjectWrapper(Object dataObject,
			int marshallerId) {
		this.dataObject = dataObject;
		this.marshallerId = marshallerId;
	}

	/**
	 * Retrieves the wrapped data object.
	 * 
	 * @return
	 */
	public Object getDataObject() {
		return dataObject;
	}

	/**
	 * Retrieves the id of the marshaller, which knows how to unmarshall the
	 * wrapped data object.
	 * 
	 * @return
	 */
	public int getMarshallerId() {
		return marshallerId;
	}
}
