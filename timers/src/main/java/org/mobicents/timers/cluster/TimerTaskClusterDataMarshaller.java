package org.mobicents.timers.cluster;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.mobicents.cluster.ClusterDataKey;
import org.mobicents.cluster.ClusterDataMarshaller;

/**
 * 
 * @author martins
 *
 */
public class TimerTaskClusterDataMarshaller implements ClusterDataMarshaller {

	public static final int ID = 3601; 
		
	@Override
	public void writeKey(ObjectOutput output, ClusterDataKey key)
			throws IOException {
		final TimerTaskClusterDataKey tKey = (TimerTaskClusterDataKey) key;
		output.writeUTF(tKey.getSchedulerName());
		// TODO optimize to not use writeObject ...
		output.writeObject(tKey.getTaskID());
	}

	@Override
	public ClusterDataKey readKey(ObjectInput input) throws IOException,
			ClassNotFoundException {
		final String schedulerName = input.readUTF();
		final Serializable taskId = (Serializable) input.readObject();
		return new TimerTaskClusterDataKey(schedulerName,taskId);
	}

	@Override
	public void writeDataObject(ObjectOutput output, Object object)
			throws IOException {
		// TODO optimize to not use writeObject, cast to timerTaskData and write fields ...
		output.writeObject(object);
	}

	@Override
	public Object readDataObject(ObjectInput input) throws IOException,
			ClassNotFoundException {
		return input.readObject();
	}

}
