package org.mobicents.cluster.infinispan;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.infinispan.remoting.transport.Address;
import org.mobicents.cluster.ClusterNodeAddress;

/**
 * Infinispan impl of {@link ClusterNodeAddress}, simply wraps the Infinispan
 * {@link Address}.
 * 
 * @author martins
 * 
 */
public class InfinispanClusterNodeAddress implements ClusterNodeAddress, Externalizable {

	private Address address;

	public InfinispanClusterNodeAddress setAddress(Address address) {
		this.address = address;
		return this;
	}
	
	/**
	 * Retrieves the wrapped address.
	 * @return
	 */
	public Address getAddress() {
		return address;
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
		InfinispanClusterNodeAddress other = (InfinispanClusterNodeAddress) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return address.toString();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if (address == null) {
			throw new IOException("null address");
		}
		out.writeObject(address);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		address = (Address) in.readObject();
	}

}
