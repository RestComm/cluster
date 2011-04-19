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

import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jgroups.Address;
import org.mobicents.cache.CacheData;
import org.mobicents.cluster.MobicentsCluster;

/**
 * 
 * Abstract class for a clustered {@link CacheData}.
 * 
 * @author martins
 *
 */
public class ClusteredCacheData extends CacheData {
	
	private final ClusteredCacheDataIndexingHandler indexingHandler;
	
	/**
	 * @param nodeFqn
	 * @param mobicentsCache
	 */
	public ClusteredCacheData(Fqn<?> nodeFqn, MobicentsCluster mobicentsCluster) {
		super(nodeFqn, mobicentsCluster.getMobicentsCache());
		indexingHandler = mobicentsCluster.getClusteredCacheDataIndexingHandler();
	}

	/* (non-Javadoc)
	 * @see org.mobicents.slee.runtime.cache.CacheData#create()
	 */
	@Override
	public boolean create() {
		if (super.create()) {
			// store local address if we are not running in local mode
			if (!getMobicentsCache().isLocalMode()) {
				setClusterNodeAddress(getMobicentsCache().getJBossCache().getLocalAddress());
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Sets the address of the cluster node, which owns the cache data
	 * @param clusterNodeAddress
	 */
	public void setClusterNodeAddress(Address clusterNodeAddress) {
		indexingHandler.setClusterNodeAddress(this,clusterNodeAddress);
	}
	
	/**
	 * Retrieves the address of the cluster node, which owns the cache data.
	 * 
	 * @return null if this data doesn't have info about the cluster node, which owns it
	 */
	public Address getClusterNodeAddress() {
		return indexingHandler.getClusterNodeAddress(this);
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.cache.CacheData#getNode()
	 */
	@Override
	protected Node getNode() {
		return super.getNode();
	}
}
