package org.mobicents.timers.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.mobicents.cluster.data.ClusterDataKey;

/**
 * 
 * @author martins
 * 
 */
public class TimerTaskClusterDataKey implements ClusterDataKey, Externalizable {

	private String taskID;
	private String schedulerName;

	public TimerTaskClusterDataKey() {}
	
	public TimerTaskClusterDataKey(String schedulerName, String taskID) {
		this.taskID = taskID;
		this.schedulerName = schedulerName;
	}

	public String getSchedulerName() {
		return schedulerName;
	}

	public String getTaskID() {
		return taskID;
	}

	@Override
	public boolean storesData() {
		return true;
	}

	@Override
	public boolean usesReferences() {
		return false;
	}

	@Override
	public boolean isFailedOver() {
		return true;
	}

	@Override
	public ClusterDataKey getListenerKey() {
		return new FaultTolerantSchedulerClusterDataKey(schedulerName);
	}

	@Override
	public int hashCode() {
		return taskID.hashCode() * 31 + schedulerName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TimerTaskClusterDataKey other = (TimerTaskClusterDataKey) obj;
		return taskID.equals(other.taskID)
				&& schedulerName.equals(other.schedulerName);
	}

	@Override
	public String toString() {
		return new StringBuilder(TimerTaskClusterDataKey.class.getSimpleName())
				.append("( ").append("taskId = ").append(taskID).append(", ")
				.append("schedulerName = ").append(schedulerName).append(" )")
				.toString();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		schedulerName = in.readUTF();
		taskID = in.readUTF();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(schedulerName);
		out.writeUTF(taskID);		
	}
	
	@Override
	public ClusterDataKey dependsOn() {
		return null;
	}
}
