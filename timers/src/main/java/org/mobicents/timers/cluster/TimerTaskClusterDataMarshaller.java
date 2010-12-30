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
public class TimerTaskClusterDataMarshaller implements ClusterDataMarshaller {

	public static final int ID = 3601; 
	
	private final TimerTaskDataMarshaller dataMarshaller;
	
	public TimerTaskClusterDataMarshaller(TimerTaskDataMarshaller dataMarshaller) {
		this.dataMarshaller = dataMarshaller;
	}
	
	
	@Override
	public void writeKey(ObjectOutput output, ClusterDataKey key)
			throws IOException {
		final TimerTaskClusterDataKey tKey = (TimerTaskClusterDataKey) key;
		output.writeUTF(tKey.getSchedulerName());
		output.writeUTF(tKey.getTaskID());
	}

	@Override
	public ClusterDataKey readKey(ObjectInput input) throws IOException,
			ClassNotFoundException {
		final String schedulerName = input.readUTF();
		final String taskId = input.readUTF();
		return new TimerTaskClusterDataKey(schedulerName,taskId);
	}

	@Override
	public void writeDataObject(ObjectOutput output, Object object)
			throws IOException {
		dataMarshaller.writeTimerTaskData(output,object);
	}

	@Override
	public Object readDataObject(ObjectInput input) throws IOException,
			ClassNotFoundException {
		return dataMarshaller.readTimerTaskData(input);
	}

}
