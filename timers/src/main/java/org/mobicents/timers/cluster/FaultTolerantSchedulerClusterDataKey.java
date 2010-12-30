package org.mobicents.timers.cluster;

import org.mobicents.cluster.data.ClusterDataKey;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantSchedulerClusterDataKey implements ClusterDataKey {

	private final String schedulerName;
	
	public FaultTolerantSchedulerClusterDataKey(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	public String getSchedulerName() {
		return schedulerName;
	}
	
	@Override
	public boolean storesData() {
		return false;
	}

	@Override
	public boolean usesReferences() {
		return true;
	}

	@Override
	public boolean isFailedOver() {
		return false;
	}

	@Override
	public int getMarshalerId() {
		return FaultTolerantSchedulerClusterDataMarshaller.ID;
	}

	@Override
	public ClusterDataKey getListenerKey() {
		return null;
	}

	@Override
	public int hashCode() {
		return schedulerName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FaultTolerantSchedulerClusterDataKey other = (FaultTolerantSchedulerClusterDataKey) obj;
		return schedulerName.equals(other.schedulerName);
	}
	
	@Override
	public String toString() {
		return new StringBuilder(
				FaultTolerantSchedulerClusterDataKey.class.getSimpleName())
		.append("( ")
		.append("schedulerName = ").append(schedulerName)
		.append(" )").toString();
	}
		
}
