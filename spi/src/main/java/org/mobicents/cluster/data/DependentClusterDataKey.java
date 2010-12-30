package org.mobicents.cluster.data;

/**
 * A key that depends on another key. This information tells the cluster how to
 * manage the related data, for instance in case of a cluster implementation,
 * which uses a consistent hash distribution model, then data of both keys
 * should be colocated.
 * 
 * Very important note, to simplify and optimize performance, it is not
 * considered that the cluster understands that key K1 depends on key K3, if K1
 * depends on K2 and K2 depends on K3. Trying such model has big chances of
 * behaving not in the expected way, in such case, to be safe, dependsOn() for
 * both K1 and K2 must return K3!
 * 
 * @author martins
 * 
 */
public interface DependentClusterDataKey extends ClusterDataKey {

	/**
	 * Retrieves the key which the dependent key depends.
	 * 
	 * @return
	 */
	public ClusterDataKey dependsOn();

}
