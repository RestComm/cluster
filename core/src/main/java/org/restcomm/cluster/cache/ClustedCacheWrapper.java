package org.restcomm.cluster.cache;

import java.io.Serializable;

import org.infinispan.remoting.transport.Address;

public class ClustedCacheWrapper<V> implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1363028114505740961L;
	private V realObject;
	private Address address;
	
	public V getRealObject() {
		return realObject;
	}
	
	public void setRealObject(V realObject) {
		this.realObject = realObject;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}	
}
