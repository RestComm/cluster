package org.mobicents.cluster.hazelcast.elector;

import java.util.List;

import org.mobicents.cluster.ClusterNodeAddress;
import org.mobicents.cluster.elector.FailoverElector;

public class HazelcastFailOverElector implements FailoverElector {

	private static final int shift = 5; // lets set default to something other than
									// zero

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.FailoverElector#elect(java.util.List)
	 */
	@Override
	public ClusterNodeAddress elect(List<ClusterNodeAddress> list) {
		// TODO ensure this works
		int size = list.size();
		int index = (shift % size) + size;
		index = index % size;
		return list.get(index);
	}

}
