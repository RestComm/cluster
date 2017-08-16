/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
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



/**
 * Simplest of elector. Use reminder of fixed index to determine master.
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * @author martins
 * @author András Kőkuti
 */
public class DefaultClusterElector implements ClusterElector{

	protected int shift = 5; // lets set default to something other than zero

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.ftf.election.SingletonElector#elect(java.util.List)
	 */
	public Address elect(List<Address> list) {
		//Jgroups return addresses always in sorted order, jbcache does not change it.
		//For buddies its ok, since we get list from failing node :) 
		// in case shift is bigger than size
		int size = list.size();
		int index = (this.shift % size) +size;
		index = index % size;

		return list.get(index);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.election.SimpleSingletonElectorMBean#getPosition()
	 */
	public int getPosition() {
		return this.shift;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.election.SimpleSingletonElectorMBean#setPosition
	 * (int)
	 */
	public void setPosition(int shift) {
		this.shift = shift;

	}

}
