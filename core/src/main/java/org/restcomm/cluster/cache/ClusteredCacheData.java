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
import org.infinispan.tree.Fqn;
import org.infinispan.tree.Node;
import org.restcomm.cache.CacheData;
import org.restcomm.cache.FqnWrapper;
import org.restcomm.cluster.MobicentsCluster;

/**
 * 
 * Abstract class for a clustered {@link CacheData}.
 * 
 * @author martins
 * @author András Kőkuti
 *
 */
public class ClusteredCacheData extends CacheData {
	
	private final ClusteredCacheDataIndexingHandler indexingHandler;
	
	/**
	 * @param nodeFqn
	 * @param mobicentsCluster
	 */
	public ClusteredCacheData(FqnWrapper nodeFqnWrapper, MobicentsCluster mobicentsCluster) {
		//super(nodeFqnWrapper.getFqn(), mobicentsCluster.getMobicentsCache());
		super(nodeFqnWrapper, mobicentsCluster.getMobicentsCache());
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
				setClusterNodeAddress(getMobicentsCache().getJBossCache().getCache().getCacheManager().getAddress());
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
	@SuppressWarnings("rawtypes")
	public Node getNode(){
		return super.getNode();
	}
}
