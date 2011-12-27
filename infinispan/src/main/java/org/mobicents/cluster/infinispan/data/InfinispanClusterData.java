package org.mobicents.cluster.infinispan.data;

import java.util.HashSet;
import java.util.Iterator;

import org.infinispan.Cache;
import org.infinispan.atomic.AtomicMap;
import org.infinispan.atomic.AtomicMapLookup;
import org.infinispan.context.Flag;
import org.infinispan.remoting.transport.Address;
import org.mobicents.cluster.ClusterNodeAddress;
import org.mobicents.cluster.data.ClusterData;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.ClusterDataSource;
import org.mobicents.cluster.infinispan.InfinispanClusterNodeAddress;

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

	private final InfinispanClusterDataSource dataSource;
	
	/**
	 * 
	 * @param key
	 * @param dataSource
	 */
	public InfinispanClusterData(ClusterDataKey key,
			InfinispanClusterDataSource dataSource) {
		this.key = key;
		this.dataSource = dataSource;
	}

	@Override
	public ClusterDataSource<?> getClusterDataSource() {
		return dataSource;
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
		return address != null ? new InfinispanClusterNodeAddress().setAddress(address)
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
		return dataSource.getWrappedDataSource().get(dataObjectKey);
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
			// store it in data source
			dataSource.getWrappedDataSource().put(dataObjectKey, value);
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
				key, InfinispanClusterDataKeyType.REFERENCES_MAP);
		// get the references atomic map
		AtomicMap<InfinispanClusterDataKey, Boolean> references = AtomicMapLookup
				.getAtomicMap(dataSource.getWrappedDataSource(),
						referencesObjectKey, false);
		if (references == null || references.isEmpty()) {
			return EMPTY_REFERENCES_RESULT;
		} else {
			HashSet<ClusterDataKey> resultSet = new HashSet<ClusterDataKey>();
			for (InfinispanClusterDataKey reference : references.keySet()) {
				resultSet.add(reference.getKey());
			}
			ClusterDataKey[] resultArray = new ClusterDataKey[resultSet.size()];
			return resultSet.toArray(resultArray);
		}
	}

	@Override
	public void initReferences() throws UnsupportedOperationException {
		// validate request
		if (!key.usesReferences()) {
			throw new UnsupportedOperationException(
					"cluster data key does not uses references");
		}
		// create specific key
		final InfinispanClusterDataKey referencesObjectKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.REFERENCES_MAP);
		// get reference's atomic map, ensuring it is created if does not exists
		final Cache cache = dataSource.getWrappedDataSource();
		AtomicMapLookup.getAtomicMap(cache,
				referencesObjectKey, true);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.ClusterData#addReference(org.mobicents.cluster.
	 * ClusterDataKey)
	 */
	@Override
	public boolean addReference(ClusterDataKey reference)
			throws UnsupportedOperationException {
		// validate request
		if (!key.usesReferences()) {
			throw new UnsupportedOperationException(
					"cluster data key does not uses references");
		}
		// create specific key
		final InfinispanClusterDataKey referencesObjectKey = new InfinispanClusterDataKey(
				key, InfinispanClusterDataKeyType.REFERENCES_MAP);
		// get reference's atomic map
		final Cache cache = dataSource.getWrappedDataSource();
		final AtomicMap references = AtomicMapLookup.getAtomicMap(cache,
				referencesObjectKey, true);
		// turn off locking
		//cache.getAdvancedCache().getInvocationContextContainer()
		//		.getInvocationContext().setFlags(Flag.SKIP_LOCKING);
		// put reference
		return references.put(new InfinispanClusterDataKey(reference,
				InfinispanClusterDataKeyType.REFERENCES_ENTRY), Boolean.TRUE) == null;
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
				key, InfinispanClusterDataKeyType.REFERENCES_MAP);
		// get reference's atomic map
		final Cache cache = dataSource.getWrappedDataSource();
		final AtomicMap references = AtomicMapLookup.getAtomicMap(cache,
				referencesObjectKey, false);
		if (references == null) {
			return false;
		}
		// references exist, turn off locking
		//cache.getAdvancedCache().getInvocationContextContainer()
		//		.getInvocationContext().setFlags(Flag.SKIP_LOCKING);
		// remove reference
		return references.remove(new InfinispanClusterDataKey(reference,
				InfinispanClusterDataKeyType.REFERENCES_ENTRY)) != null;
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
				key, InfinispanClusterDataKeyType.REFERENCES_MAP);
		// get reference's atomic map
		final AtomicMap references = AtomicMapLookup.getAtomicMap(
				dataSource.getWrappedDataSource(), referencesObjectKey, false);
		if (references == null) {
			return false;
		} else {
			return references.containsKey(new InfinispanClusterDataKey(
					reference, InfinispanClusterDataKeyType.REFERENCES_ENTRY));
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
					key, InfinispanClusterDataKeyType.REFERENCES_MAP);
			if (cascadeRemoval) {
				// cascade removal, remove also the data selected by the
				// reference keys
				AtomicMap<InfinispanClusterDataKey, Boolean> references = AtomicMapLookup
				.getAtomicMap(cache, referencesObjectKey, false);
				if (references != null) {
					ClusterDataKey reference = null;
					for (Iterator<InfinispanClusterDataKey> it = references
							.keySet().iterator(); it.hasNext();) {
						reference = it.next().getKey();					
						dataSource.getClusterData(reference).remove(true);
					}					
				}			
			}
			AtomicMapLookup.removeAtomicMap(cache, referencesObjectKey);
		}
	}

}
