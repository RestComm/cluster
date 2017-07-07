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

package org.restcomm.cluster;


import org.infinispan.remoting.transport.Address;
import org.restcomm.cluster.cache.ClusteredCacheData;
import org.restcomm.cluster.election.ClientLocalListenerElector;

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
	@SuppressWarnings("rawtypes")
	public void wonOwnership(ClusteredCacheData clusteredCacheData);

	/**
	 * Notifies the local client that it lost ownership of the specified {@link ClusteredCacheData}.
	 * @param clusteredCacheData
	 */
	@SuppressWarnings("rawtypes")
	public void lostOwnership(ClusteredCacheData clusteredCacheData);
}
