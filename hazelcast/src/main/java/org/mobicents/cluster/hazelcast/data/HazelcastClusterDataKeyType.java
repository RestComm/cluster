package org.mobicents.cluster.hazelcast.data;

/**
 * Enum representing type of real infinispan cluster data keys.
 * 
 * Note that the ordinal is used in serialzation, this means that old values
 * should never be removed or order of elements be changed, consider this a
 * "primary key" of data, there are no changes of id or data may become corrupt.
 * 
 * @author martins
 * 
 */
public enum HazelcastClusterDataKeyType {

	DATA, REFERENCES_ENTRY, REFERENCES_MAP, CLUSTER_NODE_ADDRESS

}
