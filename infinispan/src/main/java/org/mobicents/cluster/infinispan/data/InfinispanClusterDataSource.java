package org.mobicents.cluster.infinispan.data;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.mobicents.cluster.data.ClusterData;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.ClusterDataSource;
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

	private GlobalConfiguration globalConfiguration;
	private Configuration configuration;
	private MBeanServer mBeanServer;
	
	private Cache cache;
	private boolean started;
	
	/**
	 * 
	 * @param globalConfiguration
	 * @param configuration
	 * @param mBeanServer
	 */
	public InfinispanClusterDataSource(GlobalConfiguration globalConfiguration,
			Configuration configuration, MBeanServer mBeanServer) {
		this.globalConfiguration = globalConfiguration;
		this.configuration = configuration;
		this.mBeanServer = mBeanServer;
	}

	/**
	 * 
	 * @return
	 */
	public Configuration getConfiguration() throws IllegalStateException {
		return configuration;
	}
	
	/**
	 * 
	 * @return
	 */
	public GlobalConfiguration getGlobalConfiguration() throws IllegalStateException {
		return globalConfiguration;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isLocalMode() {
		return configuration.getCacheMode() == CacheMode.LOCAL;
	}
	
	/**
	 * 
	 */
	public void startCache() {		
		synchronized (this) {
			if(started) {
				throw new IllegalStateException("datasource already started");
			}
			else {
				started = true;
			}
			// add key externalizer
			globalConfiguration.addExternalizer(new InfinispanClusterDataKeyExternalizer());
			// add mbean server
			if (mBeanServer != null) {
				globalConfiguration
						.setMBeanServerLookupInstance(new MBeanServerLookupImpl(
								mBeanServer));			
			}
			cache = new DefaultCacheManager(globalConfiguration,configuration,false).getCache();
			configuration = cache.getConfiguration();
			globalConfiguration = cache.getConfiguration().getGlobalConfiguration();
			if (cache.getStatus() != ComponentStatus.RUNNING) {
				cache.start();			
			}		
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Mobicents Infinispan DataSource started, status: "
						+ cache.getStatus() + ", mode: "
						+ cache.getConfiguration().getCacheModeString());
			}
		}		
	}

	/**
	 * 
	 */
	public void stopCache() {
		synchronized (this) {
			if(!started) {
				throw new IllegalStateException("datasource not started");
			}
			else {
				started = false;
			}
			this.cache.stop();	
			this.cache = null;
		}
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
	
}
