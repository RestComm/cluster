/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.cluster;

import java.util.List;

import org.jgroups.Address;
import org.mobicents.cache.MobicentsCache;
import org.mobicents.cluster.cache.ClusteredCacheData;
import org.mobicents.cluster.cache.ClusteredCacheDataIndexingHandler;

/**
 * 
 * @author martins
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
	 * @return the mobicents cache controlled by the cluster
	 */
	public MobicentsCache getMobicentsCache();
	
	/**
	 * Retrieves the handler to manage cluster indexing of {@link ClusteredCacheData}
	 * @return
	 */
	public ClusteredCacheDataIndexingHandler getClusteredCacheDataIndexingHandler();
}
