package org.mobicents.cluster.infinispan;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.mobicents.cluster.ClusterData;
import org.mobicents.cluster.ClusterDataKey;
import org.mobicents.cluster.ClusterDataSource;

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
	private boolean managedCache;
	private boolean localMode;

	/**
	 * 
	 * @param cacheContainer
	 * @param cacheName
	 * @param managedCache
	 */
	public InfinispanClusterDataSource(CacheContainer cacheContainer,
			String cacheName, boolean managedCache) {
		this.managedCache = managedCache;
		this.cache = cacheContainer.getCache(cacheName);
	}

	/**
	 * 
	 * @param configuration
	 */
	public InfinispanClusterDataSource(Configuration configuration) {
		managedCache = false;
		EmbeddedCacheManager cacheManager = new DefaultCacheManager(
				configuration, false);
		this.cache = cacheManager.getCache();
	}

	/**
	 * 
	 */
	public InfinispanClusterDataSource() {
		managedCache = false;
		EmbeddedCacheManager cacheManager = new DefaultCacheManager(false);
		this.cache = cacheManager.getCache();
	}

	/**
	 * 
	 */
	public void start() {
		if (cache.getConfiguration().getCacheMode() == CacheMode.LOCAL) {
			localMode = true;
		}
		if (ComponentStatus.RUNNING != cache.getStatus()) {
			cache.start();
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
