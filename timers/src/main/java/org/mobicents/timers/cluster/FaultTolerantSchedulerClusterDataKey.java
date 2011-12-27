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
public class FaultTolerantSchedulerClusterDataKey implements ClusterDataKey,Externalizable {

	private String schedulerName;

	public FaultTolerantSchedulerClusterDataKey() {}
	
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
	public Object getDataRemovalListenerID() {
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
				.append("( ").append("schedulerName = ").append(schedulerName)
				.append(" )").toString();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(schedulerName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		schedulerName = in.readUTF();
	}

	@Override
	public ClusterDataKey dependsOn() {
		return null;
	}
}
