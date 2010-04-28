package org.mobicents.cluster.election;

import java.util.List;

import org.jgroups.Address;
import org.mobicents.cluster.cache.ClusteredCacheData;

public interface ClientLocalListenerElector {

	/**
	 * 
	 * @param nodes
	 * @param cacheData
	 * @return
	 */
	public Address elect(List<Address> nodes, ClusteredCacheData cacheData);
}
