package org.mobicents.cluster.base;

import org.mobicents.cluster.data.ClusterDataSource;
import org.mobicents.cluster.data.marshall.ClusterDataMarshallerManagement;

/**
 * Abstract base impl for a cluster data source, providing with the data marshaller management.
 * 
 * @author martins
 *
 * @param <T>
 */
public abstract class AbstractClusterDataSource<T> implements ClusterDataSource<T> {

	/**
	 * manager of marshallers
	 */
	protected final ClusterDataMarshallerManagement marshallerManagement = new DefaultClusterDataMarshallerManagement();

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.ClusterDataSource#getClusterDataMarshalerManagement()
	 */
	@Override
	public ClusterDataMarshallerManagement getClusterDataMarshalerManagement() {
		return marshallerManagement;
	}
	
}
