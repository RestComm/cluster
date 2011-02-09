package org.mobicents.cluster.data.marshall;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Handles serialization of specific object(s).
 * 
 * @author martins
 * 
 */
public interface ClusterDataMarshaller<T> {

	/**
	 * Writes data to output.
	 * 
	 * @param output
	 * @param object
	 * @throws IOException
	 */
	public void writeData(ObjectOutput output, T data) throws IOException;

	/**
	 * Reads data from input.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public T readData(ObjectInput input) throws IOException,
			ClassNotFoundException;

	/**
	 * Retrieves the unique ID that identifies the Marshaller.
	 * 
	 * @return
	 */
	public Integer getMarshallerID();

	/**
	 * Retrieves the Java type of the key.
	 * 
	 * @return
	 */
	public Class<? extends T> getDataType();

}
