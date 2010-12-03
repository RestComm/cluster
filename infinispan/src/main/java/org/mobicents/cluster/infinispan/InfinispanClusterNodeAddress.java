package org.mobicents.cluster.infinispan;

import org.infinispan.remoting.transport.Address;
import org.mobicents.cluster.ClusterNodeAddress;

/**
 * Infinispan impl of {@link ClusterNodeAddress}, simply wraps the Infinispan
 * {@link Address}.
 * 
 * @author martins
 * 
 */
public class InfinispanClusterNodeAddress implements ClusterNodeAddress {

	private final Address address;

	/**
	 * @param address
	 */
	public InfinispanClusterNodeAddress(Address address) {
		if (address == null) {
			throw new NullPointerException("null address");
		}
		this.address = address;
	}

	/**
	 * Retrieves the wrapped address.
	 * @return
	 */
	public Address getAddress() {
		return address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return address.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final InfinispanClusterNodeAddress other = (InfinispanClusterNodeAddress) obj;
		return address.equals(other.address);
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

}
