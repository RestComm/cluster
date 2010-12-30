package org.mobicents.cluster.infinispan.data;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.mobicents.cluster.base.AbstractClusterDataSource;
import org.mobicents.cluster.data.ClusterData;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.infinispan.data.marshall.InfinispanClusterDataKeyExternalizer;
import org.mobicents.cluster.infinispan.data.marshall.InfinispanClusterDataObjectWrapperExternalizer;
import org.mobicents.cluster.infinispan.distribution.DefaultConsistentHashExt;
import org.mobicents.cluster.infinispan.util.MBeanServerLookupImpl;

/**
 * Infinispan impl for Mobicents Cluster's DataSource.
 * 
 * @author martins
 * 
 */
@SuppressWarnings("rawtypes")
public class InfinispanClusterDataSource extends
		AbstractClusterDataSource<Cache> {

	private static final Logger LOGGER = Logger
			.getLogger(InfinispanClusterDataSource.class);

	private final Cache cache;
	private boolean managedCache = false;
	private boolean localMode;

	/**
	 * 
	 * @param cacheContainer
	 * @param cacheName
	 */
	public InfinispanClusterDataSource(CacheContainer cacheContainer,
			String cacheName, MBeanServer mBeanServer) {
		super();
		cache = cacheContainer.getCache(cacheName);
		setup(cache.getConfiguration(), cache.getConfiguration()
				.getGlobalConfiguration(), mBeanServer);
	}

	public InfinispanClusterDataSource(GlobalConfiguration globalConfiguration,
			Configuration configuration, MBeanServer mBeanServer) {
		super();
		setup(configuration, globalConfiguration, mBeanServer);
		this.cache = new DefaultCacheManager(globalConfiguration,
				configuration, false).getCache();
	}

	private void setup(Configuration configuration,
			GlobalConfiguration globalConfiguration, MBeanServer mBeanServer) {
		if (configuration.getCacheMode() == CacheMode.LOCAL) {
			this.localMode = true;
		} else {
			// sets custom distributed mode consistent hashing
			configuration.setConsistentHashClass(DefaultConsistentHashExt.class
					.getName());
			// add externalizers
			globalConfiguration
					.addExternalizer(new InfinispanClusterDataKeyExternalizer(
							marshallerManagement));
			globalConfiguration
					.addExternalizer(new InfinispanClusterDataObjectWrapperExternalizer(
							marshallerManagement));
			globalConfiguration
					.addExternalizer(new DefaultConsistentHashExt.Externalizer());
			// add mbean server
			if (mBeanServer != null) {
				globalConfiguration
						.setMBeanServerLookup(new MBeanServerLookupImpl(
								mBeanServer));
			}
		}
	}

	/**
	 * 
	 */
	public void start() {
		if (ComponentStatus.RUNNING != cache.getStatus()) {
			cache.start();
			managedCache = true;
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Mobicents Infinispan DataSource started, status: "
					+ cache.getStatus() + ", mode: "
					+ cache.getConfiguration().getCacheModeString());
		}
	}

	/**
	 * 
	 */
	public void stop() {
		if (!managedCache) {
			this.cache.stop();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.cluster.ClusterDataSource#isLocalMode()
	 */
	@Override
	public boolean isLocalMode() {
		return localMode;
	}

}
