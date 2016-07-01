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

package org.mobicents.cluster;

import java.util.List;

import org.infinispan.remoting.transport.Address;
import org.mobicents.cache.MobicentsCache;
import org.mobicents.cluster.cache.ClusteredCacheData;
import org.mobicents.cluster.cache.ClusteredCacheDataIndexingHandler;

/**
 * 
 * @author martins
 * @author András Kőkuti
 *
 */
public interface MobicentsCluster {

	/**
	 * Adds the specified fail over listener.
	 * @param listener
	 */
	public boolean addFailOverListener(FailOverListener listener);
	
	/**
	 * Removes the specified fail over listener.
	 * @param listener
	 * @return
	 */
	public boolean removeFailOverListener(FailOverListener listener);
	
	/**
	 * Adds the specified data removal listener.
	 * @param listener
	 */
	public boolean addDataRemovalListener(DataRemovalListener listener);
	
	/**
	 * Removes the specified data removal listener.
	 * @param listener
	 * @return
	 */
	public boolean removeDataRemovalListener(DataRemovalListener listener);
	
	/**
	 * Retrieves the local address of the cluster node.
	 * @return
	 */
	public Address getLocalAddress();
	
	/**
	 * Retrieves the members of the cluster.
	 * @return
	 */
	public List<Address> getClusterMembers();
	
	/**
	 * Indicates if the local node is the head of the cluster. If true it is safe to asume that we can perform cluster wide operations.
	 * @return
	 */
	public boolean isHeadMember();
	
	/**
	 * Method to determine if this node is single node in the cluster.
	 * 
	 * @return <ul>
	 *         <li><b>true</b> - cache mode is local || clusterMembers == 1
	 *         <li>
	 *         <li><b>false</b> - otherwise
	 *         <li>
	 *         </ul>
	 */
	public boolean isSingleMember();
	
	/**
	 *  
	 * @return the restcomm cache controlled by the cluster
	 */
	public MobicentsCache getMobicentsCache();
	
	/**
	 * Retrieves the handler to manage cluster indexing of {@link ClusteredCacheData}
	 * @return
	 */
	public ClusteredCacheDataIndexingHandler getClusteredCacheDataIndexingHandler();

	/**
	 * Starts the cluster. This should only be invoked when all listeners are
	 * added, and when all classes needed to deserialize data in a running
	 * cluster are visible (somehow).
	 */
	public void startCluster();
	
	/**
	 * Indicates if the cluster is running or not.
	 * @return
	 */
	public boolean isStarted();
	
	/**
	 * Stops the cluster.
	 */
	public void stopCluster();
	
}
