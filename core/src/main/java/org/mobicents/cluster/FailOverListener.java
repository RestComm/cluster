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

package org.mobicents.cluster;


import org.infinispan.remoting.transport.Address;
import org.mobicents.cluster.cache.ClusteredCacheData;
import org.mobicents.cluster.election.ClientLocalListenerElector;

import org.infinispan.tree.Fqn;

/**
 * 
 * This interface defines callback methods which will be called when the local
 * cluster node looses or wins ownership on a certain {@link ClusteredCacheData}
 * .
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * @author martins
 * @author András Kőkuti
 * 
 */
public interface FailOverListener {

	/**
	 * Retrieves the base fqn the listener has interest.
	 * @return
	 */
	
	public Fqn getBaseFqn();
	
	/**
	 * Retrieves the listener's elector, used to elect the node which does
	 * failover of specific data.
	 * 
	 * @return
	 */
	public ClientLocalListenerElector getElector();
	
	/**
	 * Retrieves the priority of the listener.
	 * @return
	 */
	public byte getPriority();

	/**
	 * Indicates that it will do fail over the cluster node with the specified {@link Address}.
	 * @param address
	 */
	public void failOverClusterMember(Address address);
	
	/**
	 * Notifies the local client that it now owns the specified {@link ClusteredCacheData}. 
	 * @param clusteredCacheData
	 */
	public void wonOwnership(ClusteredCacheData clusteredCacheData);

	/**
	 * Notifies the local client that it lost ownership of the specified {@link ClusteredCacheData}.
	 * @param clusteredCacheData
	 */
	public void lostOwnership(ClusteredCacheData clusteredCacheData);
}
