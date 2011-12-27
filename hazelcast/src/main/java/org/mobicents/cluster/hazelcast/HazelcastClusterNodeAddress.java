package org.mobicents.cluster.hazelcast;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.mobicents.cluster.ClusterNodeAddress;

import com.hazelcast.core.Member;

public class HazelcastClusterNodeAddress implements ClusterNodeAddress, Externalizable {

	private String address;
	
	public String getAddress() {
		return address;
	}
	
	public HazelcastClusterNodeAddress setAddress(String address) {
		this.address = address;
		return this;
	}
	
	@Override
	public int hashCode() {
		return (address == null) ? 0 : address.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HazelcastClusterNodeAddress other = (HazelcastClusterNodeAddress) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		address = in.readUTF();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(address);
	}
	
}
