package org.mobicents.timers.cluster;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.marshall.ClusterDataMarshaller;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantSchedulerClusterDataMarshaller implements ClusterDataMarshaller {

	public static final int ID = 3600; 
		
	@Override
	public void writeKey(ObjectOutput output, ClusterDataKey key)
			throws IOException {
		// write scheduler name
		output.writeUTF(((FaultTolerantSchedulerClusterDataKey)key).getSchedulerName());
	}

	@Override
	public ClusterDataKey readKey(ObjectInput input) throws IOException,
			ClassNotFoundException {
		// read scheduler name
		return new FaultTolerantSchedulerClusterDataKey(input.readUTF());
	}

	@Override
	public void writeDataObject(ObjectOutput output, Object object)
			throws IOException {
		// not used
		throw new IOException();
	}

	@Override
	public Object readDataObject(ObjectInput input) throws IOException,
			ClassNotFoundException {
		// not used
		throw new IOException();
	}

}
