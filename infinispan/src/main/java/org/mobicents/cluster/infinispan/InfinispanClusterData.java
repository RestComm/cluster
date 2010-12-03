package org.mobicents.cluster.infinispan;

import java.util.Iterator;

import org.infinispan.Cache;
import org.infinispan.atomic.AtomicMap;
import org.infinispan.atomic.AtomicMapLookup;
import org.infinispan.context.Flag;
import org.infinispan.remoting.transport.Address;
import org.mobicents.cluster.ClusterData;
import org.mobicents.cluster.ClusterDataKey;
import org.mobicents.cluster.ClusterDataSource;
import org.mobicents.cluster.ClusterNodeAddress;

/**
 * The Infinispan facet to interact with cluster data related to a key.
 * 
 * Data may be split in 3 places, a data object, the owner cluster node and a
 * references atomic map.
 * 
 * @author martins
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class InfinispanClusterData implements ClusterData {

	private static ClusterDataKey[] EMPTY_REFERENCES_RESULT = {};

	private final ClusterDataKey key;

	private final ClusterDataSource<Cache> dataSource;

	/**
	 * 
	 * @param key
	 * @param dataSource
	 */
	public InfinispanClusterData(ClusterDataKey key,
			ClusterDataSource<Cache> dataSource) {
		this.key = key;
		this.dataSource = dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterData#getKey()
	 */
	@Override
	public ClusterDataKey getKey() {
		return key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterData#getOwner()
	 */
	@Override
	public ClusterNodeAddress getOwner() {
		// ensure we should go to cluster
		if (!key.isFailedOver()) {
			return null;
		}
		// get infinispan address using a specific key
		final Address address = (Address) dataSource.getWrappedDataSource()
				.get(new InfinispanClusterDataKey(key,
						InfinispanClusterDataKeyType.CLUSTER_NODE_ADDRESS));
		// if not null wrap it
		return address != null ? new InfinispanClusterNodeAddress(address)
				: null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterData#setOwner()
	 */
	@Override
	public void setOwner() {
		// validate request
		if (!key.isFailedOver()) {
			throw new UnsupportedOperationException(
					"cluster data key does not stores owner");
		}
		if (dataSource.isLocalMode()) {
			// not need
			return;
		}
		// create specific key
		final InfinispanClusterDataKey clusterNodeAddressKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.CLUSTER_NODE_ADDRESS);
		// store the infinispan address, should have optimal marshalling?
		dataSource.getWrappedDataSource().put(
				clusterNodeAddressKey,
				dataSource.getWrappedDataSource().getAdvancedCache()
						.getRpcManager().getTransport().getAddress());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterData#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		// ensure we should go to cluster
		if (!key.storesData()) {
			return null;
		}
		// create specific key
		final InfinispanClusterDataKey dataObjectKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.DATA);
		// get key's value in infinispan
		final InfinispanClusterDataObjectWrapper dataObjectWrapper = (InfinispanClusterDataObjectWrapper) dataSource
				.getWrappedDataSource().get(dataObjectKey);
		// unwrap original object if result not null
		return dataObjectWrapper == null ? null : dataObjectWrapper
				.getDataObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterData#setDataObject(java.lang.Object)
	 */
	@Override
	public void setDataObject(Object value)
			throws UnsupportedOperationException {
		// validate request
		if (!key.storesData()) {
			throw new UnsupportedOperationException(
					"cluster data key does not stores data");
		}
		// create specific key
		final InfinispanClusterDataKey dataObjectKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.DATA);
		if (value != null) {
			// store it in a wrapper that has a marshall in infinispan
			dataSource.getWrappedDataSource().put(
					dataObjectKey,
					new InfinispanClusterDataObjectWrapper(value, key
							.getMarshalerId()));
		} else {
			// remove it
			dataSource.getWrappedDataSource().remove(dataObjectKey);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterData#getReferences()
	 */
	@Override
	public ClusterDataKey[] getReferences() {
		// ensure we should go to cluster
		if (!key.usesReferences()) {
			return EMPTY_REFERENCES_RESULT;
		}
		// create specific key
		final InfinispanClusterDataKey referencesObjectKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.REFERENCES);
		// get the references atomic map
		AtomicMap<ClusterDataKey, Boolean> references = AtomicMapLookup
				.getAtomicMap(dataSource.getWrappedDataSource(),
						referencesObjectKey, false);
		if (references == null) {
			return EMPTY_REFERENCES_RESULT;
		} else {
			return references.keySet().toArray(EMPTY_REFERENCES_RESULT);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.ClusterData#addReference(org.mobicents.cluster.
	 * ClusterDataKey)
	 */
	@Override
	public void addReference(ClusterDataKey reference)
			throws UnsupportedOperationException {
		// validate request
		if (!key.usesReferences()) {
			throw new UnsupportedOperationException(
					"cluster data key does not uses references");
		}
		// create specific key
		final InfinispanClusterDataKey referencesObjectKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.REFERENCES);
		// get reference's atomic map
		final Cache cache = dataSource.getWrappedDataSource();
		final AtomicMap references = AtomicMapLookup.getAtomicMap(cache,
				referencesObjectKey, true);
		// turn off locking
		cache.getAdvancedCache().getInvocationContextContainer()
				.getInvocationContext().setFlags(Flag.SKIP_LOCKING);
		// put reference
		references.put(reference, Boolean.TRUE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.ClusterData#removeReference(org.mobicents.cluster
	 * .ClusterDataKey)
	 */
	@Override
	public boolean removeReference(ClusterDataKey reference) {
		// ensure we should go to cluster
		if (!key.usesReferences()) {
			return false;
		}
		// create specific key
		final InfinispanClusterDataKey referencesObjectKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.REFERENCES);
		// get reference's atomic map
		final Cache cache = dataSource.getWrappedDataSource();
		final AtomicMap references = AtomicMapLookup.getAtomicMap(cache,
				referencesObjectKey, false);
		if (references == null) {
			return false;
		}
		// references exist, turn off locking
		cache.getAdvancedCache().getInvocationContextContainer()
				.getInvocationContext().setFlags(Flag.SKIP_LOCKING);
		// remove reference
		return references.remove(reference) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.ClusterData#containsReference(org.mobicents.cluster
	 * .ClusterDataKey)
	 */
	@Override
	public boolean containsReference(ClusterDataKey reference) {
		// ensure we should go to cluster
		if (!key.usesReferences()) {
			return false;
		}
		// create specific key
		final InfinispanClusterDataKey referencesObjectKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.REFERENCES);
		// get reference's atomic map
		final AtomicMap references = AtomicMapLookup.getAtomicMap(
				dataSource.getWrappedDataSource(), referencesObjectKey, false);
		if (references == null) {
			return false;
		} else {
			return references.containsKey(reference);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterData#remove(boolean)
	 */
	@Override
	public void remove(boolean cascadeRemoval) {
		final Cache cache = dataSource.getWrappedDataSource();
		// if needed remove data object
		if (key.storesData()) {
			cache.remove(new InfinispanClusterDataKey(key,
					InfinispanClusterDataKeyType.DATA));
		}
		// if needed remove cluster node address
		if (key.isFailedOver() && !dataSource.isLocalMode()) {
			cache.remove(new InfinispanClusterDataKey(key,
					InfinispanClusterDataKeyType.CLUSTER_NODE_ADDRESS));
		}
		// if needed remove references, this must be done first for each
		// reference and then for the atomic map key
		if (key.usesReferences()) {
			final InfinispanClusterDataKey referencesObjectKey = new InfinispanClusterDataKey(
					key, InfinispanClusterDataKeyType.REFERENCES);
			AtomicMap<ClusterDataKey, Boolean> references = AtomicMapLookup
					.getAtomicMap(cache, referencesObjectKey, false);
			if (references != null) {
				ClusterDataKey reference = null;
				for (Iterator<ClusterDataKey> it = references.keySet()
						.iterator(); it.hasNext();) {
					reference = it.next();
					if (cascadeRemoval) {
						// cascade removal, remove also the data selected by the
						// reference key
						dataSource.getClusterData(reference).remove(true);
					}
					it.remove();
				}
				cache.remove(referencesObjectKey);
			}
		}
	}

}
