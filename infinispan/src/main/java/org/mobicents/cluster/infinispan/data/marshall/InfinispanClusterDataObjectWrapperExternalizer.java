package org.mobicents.cluster.infinispan.data.marshall;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.infinispan.marshall.Externalizer;
import org.infinispan.marshall.Marshalls;
import org.mobicents.cluster.data.marshall.ClusterDataMarshaller;
import org.mobicents.cluster.data.marshall.ClusterDataMarshallerManagement;
import org.mobicents.cluster.infinispan.data.InfinispanClusterDataObjectWrapper;

/**
 * Marshalls data objects in Infinispan.
 * 
 * @author martins
 * 
 */
@Marshalls(typeClasses = InfinispanClusterDataObjectWrapper.class, id = ExternalizerIds.InfinispanClusterDataObjectWrapperExternalizer)
public class InfinispanClusterDataObjectWrapperExternalizer implements
		Externalizer<InfinispanClusterDataObjectWrapper> {

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
	public InfinispanClusterDataObjectWrapper readObject(ObjectInput objectInput)
			throws IOException, ClassNotFoundException {
		int marshallerId = objectInput.readInt();
		final ClusterDataMarshaller marshaller = marshallerManagement
				.get(marshallerId);
		if (marshaller == null) {
			throw new IOException("marshaller with id " + marshallerId
					+ " not found.");
		}
		return new InfinispanClusterDataObjectWrapper(
				marshaller.readDataObject(objectInput), marshallerId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.infinispan.marshall.Externalizer#writeObject(java.io.ObjectOutput,
	 * java.lang.Object)
	 */
	@Override
	public void writeObject(ObjectOutput objectOutput,
			InfinispanClusterDataObjectWrapper dataObjectWrapper)
			throws IOException {
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
