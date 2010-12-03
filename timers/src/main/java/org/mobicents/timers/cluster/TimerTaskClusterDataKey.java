package org.mobicents.timers.cluster;

import java.io.Serializable;

import org.mobicents.cluster.ClusterDataKey;

/**
 * 
 * @author martins
 *
 */
public class TimerTaskClusterDataKey implements ClusterDataKey {

	private final Serializable taskID;
	private final String schedulerName;
	
	public TimerTaskClusterDataKey(String schedulerName, Serializable taskID) {
		this.taskID = taskID;
		this.schedulerName = schedulerName;
	}
	
	public String getSchedulerName() {
		return schedulerName;
	}
	
	public Serializable getTaskID() {
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
	public int getMarshalerId() {
		return TimerTaskClusterDataMarshaller.ID;
	}

	@Override
	public ClusterDataKey getListenerKey() {
		return new FaultTolerantSchedulerClusterDataKey(schedulerName);
	}

	@Override
	public int hashCode() {
		return taskID.hashCode()*31+schedulerName.hashCode();
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
		return taskID.equals(other.taskID) && schedulerName.equals(other.schedulerName);
	}
	
}
