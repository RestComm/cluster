package org.mobicents.cluster.hazelcast.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.mobicents.cluster.data.ClusterDataKey;

/**
 * Key wrapper for Infinispan, adds a type for the wrapped key to target each
 * part of the cluster data.
 * 
 * @author martins
 * 
 */
public class HazelcastClusterDataKey implements Externalizable {

	private ClusterDataKey key;
	private HazelcastClusterDataKeyType type;

	public HazelcastClusterDataKey() {
	}

	/**
	 * 
	 * @param key
	 * @param type
	 */
	public HazelcastClusterDataKey(ClusterDataKey key,
			HazelcastClusterDataKeyType type) {
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
	public HazelcastClusterDataKeyType getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return key.dependsOn() != null ? key.dependsOn().hashCode() : key
				.hashCode();
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
		HazelcastClusterDataKey other = (HazelcastClusterDataKey) obj;
		if (!key.equals(other.key))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		// write the type's ordinal
		objectOutput.write(type.ordinal());
		// write the wrapped key
		objectOutput.writeObject(key);
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException,
			ClassNotFoundException {
		// read type
		final int typeOrdinal = objectInput.read();
		type = HazelcastClusterDataKeyType.values()[typeOrdinal];
		// read wrapped key
		key = (ClusterDataKey) objectInput.readObject();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+"[ key = "+key+", type = "+type+" ]";
	}
}
