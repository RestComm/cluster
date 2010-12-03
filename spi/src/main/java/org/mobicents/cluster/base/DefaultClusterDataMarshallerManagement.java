package org.mobicents.cluster.base;

import java.util.HashMap;

import org.mobicents.cluster.ClusterDataMarshaller;
import org.mobicents.cluster.ClusterDataMarshallerManagement;

/**
 * Default impl of {@link ClusterDataMarshallerManagement}. Uses a
 * {@link HashMap} to store the marshallers.
 * 
 * @author martins
 * 
 */
public class DefaultClusterDataMarshallerManagement implements
		ClusterDataMarshallerManagement {

	/**
	 * marshallers map
	 */
	private final HashMap<Integer, ClusterDataMarshaller> marshallers = new HashMap<Integer, ClusterDataMarshaller>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterDataMarshallerManagement#register(int,
	 * org.mobicents.cluster.ClusterDataMarshaller)
	 */
	@Override
	public void register(int marshallerId, ClusterDataMarshaller marshaller) {
		synchronized (marshallers) {
			if (marshallers.containsKey(Integer.valueOf(marshallerId))) {
				throw new IllegalArgumentException(
						"there is a marshaller already with generated id "
								+ marshallerId);
			}
			marshallers.put(Integer.valueOf(marshallerId), marshaller);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.ClusterDataMarshallerManagement#unregister(int)
	 */
	@Override
	public void unregister(int marshalerId) {
		marshallers.remove(Integer.valueOf(marshalerId));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterDataMarshallerManagement#get(int)
	 */
	@Override
	public ClusterDataMarshaller get(int marshalerId) {
		return marshallers.get(Integer.valueOf(marshalerId));
	}

}
