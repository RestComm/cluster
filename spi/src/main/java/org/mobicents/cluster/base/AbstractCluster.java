package org.mobicents.cluster.base;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.mobicents.cluster.Cluster;
import org.mobicents.cluster.ClusterDataFailOverListener;
import org.mobicents.cluster.ClusterDataKey;
import org.mobicents.cluster.ClusterDataMarshallerManagement;
import org.mobicents.cluster.ClusterDataRemovalListener;
import org.mobicents.cluster.ClusterDataSource;
import org.mobicents.cluster.ClusterNodeAddress;

/**
 * Base impl for {@link Cluster}.
 * 
 * @author martins
 * 
 * @param <T>
 */
public abstract class AbstractCluster<T> implements Cluster<T> {

	/**
	 * the datasource
	 */
	protected final ClusterDataSource<T> clusterDataSource;

	/**
	 * sorted set of fail over listeners
	 */
	protected final SortedSet<ClusterDataFailOverListener> failOverListeners = Collections
			.synchronizedSortedSet(new TreeSet<ClusterDataFailOverListener>(
					new ClusterDataFailOverListenerComparator()));

	/**
	 * map of data reoval listeners
	 */
	protected final ConcurrentHashMap<ClusterDataKey, ClusterDataRemovalListener> dataRemovalListeners = new ConcurrentHashMap<ClusterDataKey, ClusterDataRemovalListener>();

	/**
	 * manager of marshallers
	 */
	protected final ClusterDataMarshallerManagement marshallerManagement = new DefaultClusterDataMarshallerManagement();

	/**
	 * 
	 * @param clusterDataSource
	 */
	public AbstractCluster(ClusterDataSource<T> clusterDataSource) {
		this.clusterDataSource = clusterDataSource;
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.Cluster#isHeadMember()
	 */
	@Override
	public boolean isHeadMember() {
		if (clusterDataSource.isLocalMode()) {
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
	public boolean isSingleMember() {
		if (clusterDataSource.isLocalMode()) {
			return true;
		} else {
			return getClusterMembers().size() == 1;
		}
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
		return dataRemovalListeners.putIfAbsent(listener.getListenerKey(),
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
			if (failOverListener.getListenerKey().equals(
					listener.getListenerKey())) {
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
		return dataRemovalListeners.remove(listener.getListenerKey()) != null;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.Cluster#getClusterDataSource()
	 */
	@Override
	public ClusterDataSource<T> getClusterDataSource() {
		return clusterDataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.Cluster#getClusterDataMarshalerManagement()
	 */
	@Override
	public ClusterDataMarshallerManagement getClusterDataMarshalerManagement() {
		return marshallerManagement;
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
