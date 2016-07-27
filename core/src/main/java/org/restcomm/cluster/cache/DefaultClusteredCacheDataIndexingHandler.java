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

/**
 * Default impl for cluster cache data indexing, which relies on storing a data
 * field with the cluster node address.
 * 
 * @author martins
 * @author András Kőkuti
 * 
 */
public class DefaultClusteredCacheDataIndexingHandler implements ClusteredCacheDataIndexingHandler {
	
	/**
	 * 
	 */
	private static final String CLUSTER_NODE_ADDRESS_NODE_KEY = "cnaddress";
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.cache.ClusteredCacheDataIndexingHandler#setClusterNodeAddress(org.mobicents.cluster.cache.ClusteredCacheData, org.jgroups.Address)
	 */
	@SuppressWarnings("unchecked")
	public void setClusterNodeAddress(ClusteredCacheData cacheData, Address clusterNodeAddress) {
		cacheData.getNode().put(CLUSTER_NODE_ADDRESS_NODE_KEY,clusterNodeAddress);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.cache.ClusteredCacheData#getClusterNodeAddress()
	 */
	@SuppressWarnings("unchecked")
	public Address getClusterNodeAddress(ClusteredCacheData cacheData) {
		return (Address) cacheData.getNode().get(CLUSTER_NODE_ADDRESS_NODE_KEY);
	}
}
