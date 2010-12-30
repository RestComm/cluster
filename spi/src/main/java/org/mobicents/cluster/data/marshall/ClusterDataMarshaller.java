package org.mobicents.cluster.data.marshall;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.mobicents.cluster.data.ClusterDataKey;

/**
 * Handles serialization of specific object(s).
 * @author martins
 *
 */
public interface ClusterDataMarshaller {

	/**
	 * Writes a key to output.
	 * @param output
	 * @param key
	 * @throws IOException
	 */
	public void writeKey(ObjectOutput output, ClusterDataKey key) throws IOException;
	
	/**
	 * Reads a key from input.
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public ClusterDataKey readKey(ObjectInput input) throws IOException, ClassNotFoundException;
	
	/**
	 * Writes a data object to output.
	 * @param output
	 * @param object
	 * @throws IOException
	 */
	public void writeDataObject(ObjectOutput output, Object object) throws IOException;
	
	/**
	 * Reads a data object from input.
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object readDataObject(ObjectInput input) throws IOException, ClassNotFoundException;

	@Override
	public int hashCode();
	
	@Override
	public boolean equals(Object obj);
}
