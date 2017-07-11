/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.restcomm.cluster.cache;


import org.infinispan.remoting.transport.Address;
import org.restcomm.cache.CacheData;
import org.restcomm.cluster.MobicentsCluster;

/**
 * 
 * Abstract class for a clustered {@link CacheData}.
 * 
 * @author martins
 * @author András Kőkuti
 *
 */
public class ClusteredCacheData<K,V> extends CacheData<K,ClustedCacheWrapper<V>> {
	
	/**
	 * @param nodeFqn
	 * @param mobicentsCluster
	 */
	public ClusteredCacheData(K key, MobicentsCluster mobicentsCluster) {
		super(key, mobicentsCluster.getMobicentsCache());		
	}

	public boolean create() {
		if (!isLocal()) {
			setClusterNodeAddress(getCacheManager().getAddress());
		}
		return true;
	}
	
	/**
	 * Sets the address of the cluster node, which owns the cache data
	 * @param clusterNodeAddress
	 */
	public void setClusterNodeAddress(Address clusterNodeAddress) {
		ClustedCacheWrapper<V> wrappedData=(ClustedCacheWrapper<V>)get();
		ClustedCacheWrapper<V> newData=new ClustedCacheWrapper<V>();
		newData.setAddress(clusterNodeAddress);
		if(wrappedData!=null && wrappedData.getRealObject()!=null)
			newData.setRealObject(wrappedData.getRealObject());
		
		super.put(newData);		
	}
	
	/**
	 * Retrieves the address of the cluster node, which owns the cache data.
	 * 
	 * @return null if this data doesn't have info about the cluster node, which owns it
	 */
	public Address getClusterNodeAddress() {
		ClustedCacheWrapper<V> wrappedData=(ClustedCacheWrapper<V>)get();
		if(wrappedData==null)
			return null;
				
		return wrappedData.getAddress();
	}
	
	public V getValue()
	{
		ClustedCacheWrapper<V> wrappedData=(ClustedCacheWrapper<V>)super.get();
		if(wrappedData==null)
			return null;
		
		return wrappedData.getRealObject();				
	}
	
	public V putValue(V value)
	{
		ClustedCacheWrapper<V> wrappedData=(ClustedCacheWrapper<V>)get();
		ClustedCacheWrapper<V> newData=new ClustedCacheWrapper<V>();
		newData.setRealObject(value);
		if(wrappedData!=null && wrappedData.getAddress()!=null)
			newData.setAddress(wrappedData.getAddress());
		
		super.put(newData);
		if(wrappedData!=null)
			return wrappedData.getRealObject();
		
		return null;
	}
	
	public V removeElement()
	{
		ClustedCacheWrapper<V> wrappedData=super.remove();
		if(wrappedData!=null)
			return wrappedData.getRealObject();
		
		return null;
	}
}