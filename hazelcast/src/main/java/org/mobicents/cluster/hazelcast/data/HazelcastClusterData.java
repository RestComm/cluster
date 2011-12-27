package org.mobicents.cluster.hazelcast.data;

import java.util.Collection;

import org.mobicents.cluster.ClusterNodeAddress;
import org.mobicents.cluster.data.ClusterData;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.ClusterDataSource;
import org.mobicents.cluster.hazelcast.HazelcastClusterNodeAddress;

import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;

/**
 * The Infinispan facet to interact with cluster data related to a key.
 * 
 * Data may be split in 3 places, a data object, the owner cluster node and a
 * references atomic map.
 * 
 * @author martins
 * 
 */
//@SuppressWarnings({ "unchecked", "rawtypes" })
public class HazelcastClusterData implements ClusterData {

	private static ClusterDataKey[] EMPTY_REFERENCES_RESULT = {};

	private final ClusterDataKey key;

	private final HazelcastClusterDataSource dataSource;
	
	/**
	 * 
	 * @param key
	 * @param dataSource
	 */
	public HazelcastClusterData(ClusterDataKey key,
			HazelcastClusterDataSource dataSource) {
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
		// get member address using a specific key
		final String address = (String) dataSource.getMap().get(new HazelcastClusterDataKey(key, HazelcastClusterDataKeyType.CLUSTER_NODE_ADDRESS));
		// if not null wrap it
		return address != null ? new HazelcastClusterNodeAddress().setAddress(address)
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
		// create specific key
		final HazelcastClusterDataKey clusterNodeAddressKey = new HazelcastClusterDataKey(key, HazelcastClusterDataKeyType.CLUSTER_NODE_ADDRESS);
		// store the infinispan address, should have optimal marshalling?
		dataSource.getMap().put(
				clusterNodeAddressKey,
				dataSource.getWrappedDataSource().getCluster().getLocalMember());
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
		final HazelcastClusterDataKey dataObjectKey = new HazelcastClusterDataKey(
				key, HazelcastClusterDataKeyType.DATA);
		// get key's value in infinispan
		return dataSource.getMap().get(dataObjectKey);
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
		final HazelcastClusterDataKey dataObjectKey = new HazelcastClusterDataKey(
				key, HazelcastClusterDataKeyType.DATA);
		if (value != null) {
			// store it in data source
			dataSource.getMap().put(dataObjectKey, value);
		} else {
			// remove it
			dataSource.getMap().remove(dataObjectKey);
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
		
		// get the references from multi map
		MultiMap<ClusterDataKey, ClusterDataKey> multiMap = dataSource.getMultiMap();
		Collection<ClusterDataKey> references = multiMap.get(key);
		if (references == null || references.isEmpty()) {
			return EMPTY_REFERENCES_RESULT;
		} else {
			ClusterDataKey[] resultArray = new ClusterDataKey[references.size()];
			return references.toArray(resultArray);
		}
	}

	@Override
	public void initReferences() throws UnsupportedOperationException {
		// nothing to do
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
		MultiMap<ClusterDataKey, ClusterDataKey> multiMap = dataSource.getMultiMap();
		return multiMap.put(key, reference);
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
		MultiMap<ClusterDataKey, ClusterDataKey> multiMap = dataSource.getMultiMap();
		return multiMap.remove(key, reference);
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
		MultiMap<ClusterDataKey, ClusterDataKey> multiMap = dataSource.getMultiMap();
		return multiMap.containsEntry(key, reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterData#remove(boolean)
	 */
	@Override
	public void remove(boolean cascadeRemoval) {
		IMap<HazelcastClusterDataKey,?> map = null;
		// if needed remove data object
		if (key.storesData()) {
			map = dataSource.getMap();
			map.remove(new HazelcastClusterDataKey(key,
					HazelcastClusterDataKeyType.DATA));
		}
		// if needed remove cluster node address
		if (key.isFailedOver()) {
			if(map == null) {
				map = dataSource.getMap();
			}
			map.remove(new HazelcastClusterDataKey(key,
					HazelcastClusterDataKeyType.CLUSTER_NODE_ADDRESS));
		}
		// if needed remove references, this must be done first for each
		// reference and then for the atomic map key
		if (key.usesReferences()) {
			MultiMap<ClusterDataKey, ClusterDataKey> multiMap = dataSource.getMultiMap();
			if (cascadeRemoval) {
				// cascade removal, remove also the data selected by the
				// reference keys
				Collection<ClusterDataKey> references = multiMap.get(key);
				if (references != null) {
					for (ClusterDataKey reference : references) {
						dataSource.getClusterData(reference).remove(true);
					}					
				}			
			}
			multiMap.remove(key);
		}
	}

}
