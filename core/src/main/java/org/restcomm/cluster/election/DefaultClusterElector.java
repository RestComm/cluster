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

package org.restcomm.cluster.election;

import java.util.List;

import org.jgroups.Address;

/**
 * Simplest of elector. Use reminder of fixed index to determine master.
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * @author martins
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
