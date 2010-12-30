package org.mobicents.cluster.infinispan.data.marshall;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.infinispan.marshall.Externalizer;
import org.infinispan.marshall.Marshalls;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.marshall.ClusterDataMarshaller;
import org.mobicents.cluster.data.marshall.ClusterDataMarshallerManagement;
import org.mobicents.cluster.infinispan.data.InfinispanClusterDataKey;
import org.mobicents.cluster.infinispan.data.InfinispanClusterDataKeyType;

/**
 * Marshalls cluster data keys in Infinispan.
 * 
 * @author martins
 * 
 */
@Marshalls(typeClasses = InfinispanClusterDataKey.class, id = ExternalizerIds.InfinispanClusterDataKeyExternalizer)
public class InfinispanClusterDataKeyExternalizer implements
		Externalizer<InfinispanClusterDataKey> {

	private final ClusterDataMarshallerManagement marshallerManagement;

	/**
	 * 
	 * @param marshallerManagement
	 */
	public InfinispanClusterDataKeyExternalizer(
			ClusterDataMarshallerManagement marshallerManagement) {
		this.marshallerManagement = marshallerManagement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.infinispan.marshall.Externalizer#readObject(java.io.ObjectInput)
	 */
	@Override
	public InfinispanClusterDataKey readObject(ObjectInput objectInput)
			throws IOException, ClassNotFoundException {
		// read marshaller id
		int marshallerId = objectInput.readInt();
		final ClusterDataMarshaller marshaller = marshallerManagement
				.get(marshallerId);
		if (marshaller == null) {
			throw new IOException("marshaller with id " + marshallerId
					+ " not found.");
		}
		// read type
		final int typeOrdinal = objectInput.read();
		final InfinispanClusterDataKeyType type = InfinispanClusterDataKeyType
				.values()[typeOrdinal];
		// read wrapped key
		final ClusterDataKey key = marshaller.readKey(objectInput);
		// build and return result
		return new InfinispanClusterDataKey(key, type);
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
			InfinispanClusterDataKey infinispanKey) throws IOException {
		final ClusterDataMarshaller marshaller = marshallerManagement
				.get(infinispanKey.getKey().getMarshalerId());
		if (marshaller == null) {
			throw new IOException("marshaller with id "
					+ infinispanKey.getKey().getMarshalerId() + " not found.");
		}
		objectOutput.writeInt(infinispanKey.getKey().getMarshalerId());
		// write the type's ordinal
		objectOutput.write(infinispanKey.getType().ordinal());
		// write the wrapped key
		marshaller.writeKey(objectOutput, infinispanKey.getKey());
	}

}
