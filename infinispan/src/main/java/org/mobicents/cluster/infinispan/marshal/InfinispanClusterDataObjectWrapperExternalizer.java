package org.mobicents.cluster.infinispan.marshal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.infinispan.marshall.Externalizer;
import org.mobicents.cluster.ClusterDataMarshaller;
import org.mobicents.cluster.ClusterDataMarshallerManagement;
import org.mobicents.cluster.infinispan.InfinispanClusterDataObjectWrapper;

/**
 * Marshalls data objects in Infinispan.
 * 
 * @author martins
 * 
 */
public class InfinispanClusterDataObjectWrapperExternalizer implements
		Externalizer {

	/**
	 * the infinispan marshall id
	 */
	public static final int ID = -200;

	private final ClusterDataMarshallerManagement marshallerManagement;

	/**
	 * 
	 * @param marshallerManagement
	 */
	public InfinispanClusterDataObjectWrapperExternalizer(
			ClusterDataMarshallerManagement marshallerManagement) {
		this.marshallerManagement = marshallerManagement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.infinispan.marshall.Externalizer#readObject(java.io.ObjectInput)
	 */
	@Override
	public Object readObject(ObjectInput objectInput) throws IOException,
			ClassNotFoundException {
		int marshallerId = objectInput.readInt();
		final ClusterDataMarshaller marshaller = marshallerManagement
				.get(marshallerId);
		if (marshaller == null) {
			throw new IOException("marshaller with id " + marshallerId
					+ " not found.");
		}
		return marshaller.readDataObject(objectInput);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.infinispan.marshall.Externalizer#writeObject(java.io.ObjectOutput,
	 * java.lang.Object)
	 */
	@Override
	public void writeObject(ObjectOutput objectOutput, Object object)
			throws IOException {
		InfinispanClusterDataObjectWrapper dataObjectWrapper = (InfinispanClusterDataObjectWrapper) object;
		final ClusterDataMarshaller marshaller = marshallerManagement
				.get(dataObjectWrapper.getMarshallerId());
		if (marshaller == null) {
			throw new IOException("marshaller with id "
					+ dataObjectWrapper.getMarshallerId() + " not found.");
		}
		objectOutput.writeInt(dataObjectWrapper.getMarshallerId());
		marshaller.writeDataObject(objectOutput,
				dataObjectWrapper.getDataObject());
	}

}
