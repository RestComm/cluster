package org.mobicents.cluster.infinispan.data.marshall;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.marshall.Externalizer;
import org.mobicents.cluster.data.marshall.ClusterDataMarshaller;

public class ClusterDataExternalizer<T> implements Externalizer<T> {

	private final ClusterDataMarshaller<T> marshaller;
	
	private final Set<Class<? extends T>> dataTypes;
	
	public ClusterDataExternalizer(ClusterDataMarshaller<T> marshaller) {
		this.marshaller = marshaller;
		dataTypes = new HashSet<Class<? extends T>>();
		dataTypes.add(marshaller.getDataType());
	}
	
	@Override
	public Integer getId() {
		return marshaller.getMarshallerID();
	}

	@Override
	public Set<Class<? extends T>> getTypeClasses() {
		return dataTypes;
	}

	@Override
	public T readObject(ObjectInput input) throws IOException,
			ClassNotFoundException {
		return marshaller.readData(input);
	}

	@Override
	public void writeObject(ObjectOutput output, T data) throws IOException {
		marshaller.writeData(output, data);		
	}

}
