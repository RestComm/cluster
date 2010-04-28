package org.mobicents.cluster;

import org.jboss.cache.Fqn;

/**
 * 
 * @author martins
 *
 */
public interface DataRemovalListener {

	/**
	 * Retrieves the base fqn the listener has interest.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Fqn getBaseFqn();
	
	/**
	 * Indicates that the data with the specified fqn was removed. 
	 * @param clusteredCacheDataFqn
	 */
	@SuppressWarnings("unchecked")
	public void dataRemoved(Fqn clusteredCacheDataFqn);
}
