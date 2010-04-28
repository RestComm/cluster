/**
 * 
 */
package org.mobicents.cluster;

import java.util.Comparator;

/**
 * @author martins
 *
 */
public class FailOverListenerPriorityComparator implements Comparator<FailOverListener> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(FailOverListener o1, FailOverListener o2) {
		if (o1.equals(o2)) {
			return 0;
		}
		else {
			if (o1.getPriority() > o2.getPriority()) {
				return -1;
			}
			else {
				return 1;
			}
		}
	}

}
