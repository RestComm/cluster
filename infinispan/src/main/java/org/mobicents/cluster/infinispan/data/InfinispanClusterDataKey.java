package org.mobicents.cluster.infinispan.data;

import org.mobicents.cluster.data.ClusterDataKey;

/**
 * Key wrapper for Infinispan, adds a type for the wrapped key to target each
 * part of the cluster data.
 * 
 * @author martins
 * 
 */
public class InfinispanClusterDataKey {

	private final ClusterDataKey key;
	private final InfinispanClusterDataKeyType type;

	/**
	 * 
	 * @param key
	 * @param type
	 */
	public InfinispanClusterDataKey(ClusterDataKey key,
			InfinispanClusterDataKeyType type) {
		this.key = key;
		this.type = type;
	}

	/**
	 * Retrieves the wrapped key.
	 * 
	 * @return
	 */
	public ClusterDataKey getKey() {
		return key;
	}

	/**
	 * Retrieves the key type.
	 * 
	 * @return
	 */
	public InfinispanClusterDataKeyType getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return key.hashCode() * 31 + type.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InfinispanClusterDataKey other = (InfinispanClusterDataKey) obj;
		if (!key.equals(other.key))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
