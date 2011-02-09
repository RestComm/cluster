package org.mobicents.cluster.base.data.marshall;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.mobicents.cluster.data.marshall.ClusterDataMarshaller;

/**
 * A base abstract implementation for a Marshaller of an {@link Externalizable} type.
 * @author martins
 * 
 * @param <T>
 */
public abstract class ExternalizableMarshaller<T extends Externalizable>
		implements ClusterDataMarshaller<T> {

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.data.marshall.ClusterDataMarshaller#getDataType()
	 */
	@Override
	public abstract Class<? extends T> getDataType();

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.data.marshall.ClusterDataMarshaller#getMarshallerID()
	 */
	@Override
	public abstract Integer getMarshallerID();

	/**
	 * Retrieves an instance of the data type. Used for unmarshalling.
	 * @return
	 */
	public abstract T getDataTypeInstance();
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.data.marshall.ClusterDataMarshaller#readData(java.io.ObjectInput)
	 */
	@Override
	public T readData(ObjectInput input) throws IOException,
			ClassNotFoundException {
		//System.out.println("readData "+getDataType());
		T t = getDataTypeInstance();
		t.readExternal(input);
		return t;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.data.marshall.ClusterDataMarshaller#writeData(java.io.ObjectOutput, java.lang.Object)
	 */
	@Override
	public void writeData(ObjectOutput output, T data) throws IOException {
		//System.out.println("writeData "+getDataType());
		data.writeExternal(output);
	}
	
}
