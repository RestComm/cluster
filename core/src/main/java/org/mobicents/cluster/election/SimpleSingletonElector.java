package org.mobicents.cluster.election;

import java.util.List;

import org.jgroups.Address;

/**
 * Simplest of elector. Use reminder of fixed index to determine master.
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * @author martins
 */
public class SimpleSingletonElector implements SingletonElector{

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
