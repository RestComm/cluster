/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.cluster.cache;

import org.jgroups.Address;

/**
 * Default impl for cluster cache data indexing, which relies on storing a data
 * field with the cluster node address.
 * 
 * @author martins
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
