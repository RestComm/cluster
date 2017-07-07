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

package org.restcomm.cluster.election;

import java.util.List;

import org.infinispan.remoting.transport.Address;
import org.restcomm.cluster.cache.ClusteredCacheData;

/**
 * Listener for the node election.
 * 
 * @author martins
 * @author András Kőkuti
 *
 */
public interface ClientLocalListenerElector {

	/**
	 * 
	 * @param nodes
	 * @param cacheData
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Address elect(List<Address> nodes, ClusteredCacheData cacheData);
}
