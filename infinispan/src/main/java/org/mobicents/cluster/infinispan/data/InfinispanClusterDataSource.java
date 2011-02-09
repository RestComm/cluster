package org.mobicents.cluster.infinispan.data;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.manager.DefaultCacheManager;
import org.mobicents.cluster.data.ClusterData;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.ClusterDataSource;
import org.mobicents.cluster.data.marshall.ClusterDataMarshaller;
import org.mobicents.cluster.infinispan.data.marshall.ClusterDataExternalizer;
import org.mobicents.cluster.infinispan.data.marshall.InfinispanClusterDataKeyExternalizer;
import org.mobicents.cluster.infinispan.util.MBeanServerLookupImpl;

/**
 * Infinispan impl for Mobicents Cluster's DataSource.
 * 
 * @author martins
 * 
 */
@SuppressWarnings("rawtypes")
public class InfinispanClusterDataSource implements ClusterDataSource<Cache> {

	private static final Logger LOGGER = Logger
			.getLogger(InfinispanClusterDataSource.class);

	private final Cache cache;
	
	//private boolean managedCache = false;
	private boolean localMode = false;
	private boolean started = false;

	public InfinispanClusterDataSource(GlobalConfiguration globalConfiguration,
			Configuration configuration, MBeanServer mBeanServer) {
		if (configuration.getCacheMode() == CacheMode.LOCAL) {
			this.localMode = true;
		} else {
			// add key externalizer
			globalConfiguration
					.addExternalizer(new InfinispanClusterDataKeyExternalizer());
			// add mbean server
			if (mBeanServer != null) {
				globalConfiguration
						.setMBeanServerLookupInstance(new MBeanServerLookupImpl(
								mBeanServer));
			}
		}
		cache = new DefaultCacheManager(globalConfiguration,
				configuration, false).getCache();		
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.data.ClusterDataSource#startDatasource()
	 */
	@Override
	public void startDatasource() {		
		if (!isStarted()) {
			started = true;
			cache.start();			
		}
		else  {
			throw new IllegalStateException("Datasource already started");
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Mobicents Infinispan DataSource started, status: "
					+ cache.getStatus() + ", mode: "
					+ cache.getConfiguration().getCacheModeString());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.data.ClusterDataSource#stopDatasource()
	 */
	@Override
	public void stopDatasource() {
		if (!isStarted()) {
			throw new IllegalStateException("Datasource not started");
		}
		else {
			this.cache.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.data.ClusterDataSource#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return started;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterDataSource#getWrappedDataSource()
	 */
	@Override
	public Cache getWrappedDataSource() {
		return cache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.cluster.ClusterDataSource#getClusterData(org.mobicents.
	 * cluster.ClusterDataKey)
	 */
	@Override
	public ClusterData getClusterData(ClusterDataKey key) {
		return new InfinispanClusterData(key, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterDataSource#isLocalMode()
	 */
	@Override
	public boolean isLocalMode() {
		return localMode;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.cluster.data.ClusterDataSource#addMarshaller(org.mobicents.cluster.data.marshall.ClusterDataMarshaller)
	 */
	@Override
	public <S> void addMarshaller(ClusterDataMarshaller<S> marshaller)
			throws IllegalStateException {
		if (!isLocalMode()) {
			if (isStarted()) {
				throw new IllegalStateException(
						"Marshallers must be added with Infinispan Cache not yet started.");
			} else {
				LOGGER.info("Adding Marshaller for type "+marshaller.getDataType());
				cache.getConfiguration()
						.getGlobalConfiguration()
						.addExternalizer(
								new ClusterDataExternalizer<S>(marshaller));
			}
		}
	}	
	
}
