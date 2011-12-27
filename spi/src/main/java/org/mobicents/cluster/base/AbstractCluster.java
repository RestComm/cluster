package org.mobicents.cluster.base;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.mobicents.cluster.Cluster;
import org.mobicents.cluster.ClusterNodeAddress;
import org.mobicents.cluster.listener.ClusterDataFailOverListener;
import org.mobicents.cluster.listener.ClusterDataRemovalListener;

/**
 * Base impl for {@link Cluster}.
 * 
 * @author martins
 * 
 * @param <T>
 */
public abstract class AbstractCluster<T> implements Cluster<T> {

	/**
	 * sorted set of fail over listeners
	 */
	protected final SortedSet<ClusterDataFailOverListener> failOverListeners = Collections
			.synchronizedSortedSet(new TreeSet<ClusterDataFailOverListener>(
					new ClusterDataFailOverListenerComparator()));

	/**
	 * map of data reoval listeners
	 */
	protected final ConcurrentHashMap<Object, ClusterDataRemovalListener> dataRemovalListeners = new ConcurrentHashMap<Object, ClusterDataRemovalListener>();
	
	/**
	 * 
	 */
	protected boolean started;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.Cluster#getLocalAddress()
	 */
	@Override
	public abstract ClusterNodeAddress getLocalAddress();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.Cluster#getClusterMembers()
	 */
	@Override
	public abstract List<ClusterNodeAddress> getClusterMembers();

	/**
	 * Throws exception if cluster not started.
	 * @throws IllegalStateException
	 */
	protected void throwExceptionIfClusterNotStarted() throws IllegalStateException {
		if(!isStarted()) {
			throw new IllegalStateException("Cluster not started.");
		}
	}
	
	/**
	 * Throws exception if cluster started.
	 * @throws IllegalStateException
	 */
	protected void throwExceptionIfClusterStarted() throws IllegalStateException {
		if(isStarted()) {
			throw new IllegalStateException("Cluster already started.");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.Cluster#isHeadMember()
	 */
	@Override
	public boolean isHeadMember() throws IllegalStateException {
		throwExceptionIfClusterNotStarted();
		if (isLocalMode()) {
			return true;
		} else {
			return getClusterMembers().get(0).equals(getLocalAddress());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.Cluster#isSingleMember()
	 */
	@Override
	public boolean isSingleMember() throws IllegalStateException {
		throwExceptionIfClusterNotStarted();
		if (isLocalMode()) {
			return true;
		} else {
			return getClusterMembers().size() == 1;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.Cluster#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return started;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.Cluster#addDataRemovalListener(org.mobicents.cluster
	 * .ClusterDataRemovalListener)
	 */
	@Override
	public boolean addDataRemovalListener(ClusterDataRemovalListener listener) {
		return dataRemovalListeners.putIfAbsent(listener.getDataRemovalListenerID(),
				listener) == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.Cluster#addFailOverListener(org.mobicents.cluster
	 * .ClusterDataFailOverListener)
	 */
	@Override
	public boolean addFailOverListener(ClusterDataFailOverListener listener) {
		for (ClusterDataFailOverListener failOverListener : failOverListeners) {
			if (failOverListener.getDataFailoverListenerKey().equals(
					listener.getDataFailoverListenerKey())) {
				return false;
			}
		}
		return failOverListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.Cluster#removeDataRemovalListener(org.mobicents
	 * .cluster.ClusterDataRemovalListener)
	 */
	@Override
	public boolean removeDataRemovalListener(ClusterDataRemovalListener listener) {
		return dataRemovalListeners.remove(listener.getDataRemovalListenerID()) != null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.Cluster#removeFailOverListener(org.mobicents.cluster
	 * .ClusterDataFailOverListener)
	 */
	@Override
	public boolean removeFailOverListener(ClusterDataFailOverListener listener) {
		return failOverListeners.remove(listener);
	}

	/**
	 * default comparator for failover listeners by priority value
	 * 
	 * @author martins
	 * 
	 */
	private static class ClusterDataFailOverListenerComparator implements
			Comparator<ClusterDataFailOverListener>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(ClusterDataFailOverListener o1,
				ClusterDataFailOverListener o2) {
			if (o1.equals(o2)) {
				return 0;
			} else {
				if (o1.getPriority() > o2.getPriority()) {
					return -1;
				} else {
					return 1;
				}
			}
		}
	}

}
