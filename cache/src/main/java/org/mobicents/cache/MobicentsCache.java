/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.cache;

import java.io.IOException;

import org.apache.log4j.Logger;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.tree.TreeCache;
import org.infinispan.tree.TreeCacheFactory;

/**
 * The container's HA and FT data source.
 * 
 * @author martins
 * @author András Kőkuti
 * 
 */
public class MobicentsCache {

	private static Logger logger = Logger.getLogger(MobicentsCache.class);

	
	private final CacheContainer jBossCacheContainer;
	@SuppressWarnings("rawtypes")
	private final TreeCache jBossDefaultCache;
	
	private boolean localMode;
	private final boolean managedCache;

	
	public MobicentsCache(Configuration cacheConfiguration, GlobalConfiguration globalCacheConfiguration) {
		this.jBossCacheContainer = new DefaultCacheManager(globalCacheConfiguration, cacheConfiguration, false);
		this.jBossDefaultCache = new TreeCacheFactory().createTreeCache(this.jBossCacheContainer.getCache());
		this.managedCache = false;
		setLocalMode();
	}

	public MobicentsCache(String cacheConfigurationLocation) throws IOException {
		//this.jBossCache = new DefaultCacheFactory().createCache(cacheConfigurationLocation,false);
		this.jBossCacheContainer = new DefaultCacheManager(cacheConfigurationLocation, false);
		this.jBossDefaultCache = new TreeCacheFactory().createTreeCache(this.jBossCacheContainer.getCache());
		this.managedCache = false;
		setLocalMode();
	}
	
		
	public MobicentsCache(CacheContainer cacheContainer) {
		this.jBossCacheContainer = cacheContainer;
		this.jBossDefaultCache = new TreeCacheFactory().createTreeCache(this.jBossCacheContainer.getCache());
		this.managedCache = true;									
		setLocalMode();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MobicentsCache(Cache cache){
		this.jBossDefaultCache = new TreeCacheFactory().createTreeCache(cache);
		this.jBossCacheContainer = cache.getCacheManager();
		this.managedCache = true;
		setLocalMode();
	}
	
	private void setLocalMode() {
		if(jBossDefaultCache.getCache().getCacheConfiguration().clustering().cacheMode() == CacheMode.LOCAL){
			localMode = true;
		}
		
	}
	
	public void startCache() {
		if(!(jBossDefaultCache.getCache().getStatus() == ComponentStatus.RUNNING)){
			logger.info("Starting JBoss Cache...");
			jBossDefaultCache.start();
		}
		if (logger.isInfoEnabled()) {
			logger.info("Mobicents Cache started, status: " + this.jBossDefaultCache.getCache().getStatus() + ", Mode: " + jBossDefaultCache.getCache().getCacheConfiguration().clustering().cacheMode());
		}
	}
	
	public CacheContainer getJBossCacheContainer() {
		return jBossCacheContainer;
	}
	
	@SuppressWarnings("rawtypes")
	public TreeCache getJBossCache() {
		return jBossDefaultCache;
	}
	
	public void stopCache() {
		if (!managedCache) {
			if (logger.isInfoEnabled()) {
				logger.info("Mobicents Cache stopping...");
			}			
			this.jBossCacheContainer.stop();
			//this.jBossCacheManager.destroy();
		}
		if (logger.isInfoEnabled()) {
			logger.info("Mobicents Cache stopped.");
		}		
	}

	/**
	 * Indicates if the cache is not in a cluster environment. 
	 * @return the localMode
	 */
	public boolean isLocalMode() {
		return localMode;
	}
	
	/**
	 * Sets the class loader to be used on serialization operations, for data
	 * stored in the specified fqn and child nodes. Note that if another class
	 * loader is set for a specific child node tree, the cache will use instead
	 * that class loader.
	 * 
	 * @param regionFqn
	 * @param classLoader
	 */
	/*@SuppressWarnings("rawtypes")
	public void setReplicationClassLoader(Fqn regionFqn, ClassLoader classLoader) {
		if (!isLocalMode()) {
			final Region region = jBossCache.getRegion(regionFqn, true);
			region.registerContextClassLoader(classLoader);
			if (!region.isActive() && jBossCache.getCacheStatus() == CacheStatus.STARTED) {
				region.activate();
			}			
		}
	}*/
	
	/**
	 * Sets the class loader to be used on serialization operations, for all
	 * data stored. Note that if another class loader is set for a specific
	 * child node tree, the cache will use instead that class loader.
	 * 
	 * @param classLoader
	 */
	/*public void setReplicationClassLoader(ClassLoader classLoader) {
		setReplicationClassLoader(Fqn.ROOT, classLoader);
	}*/
	
	/**
	 * Unsets the class loader to be used on serialization operations, for data
	 * stored in the specified fqn and child nodes.
	 * @param regionFqn
	 * @param classLoader
	 */
	/*@SuppressWarnings("rawtypes")
	public void unsetReplicationClassLoader(Fqn regionFqn, ClassLoader classLoader) {
		if (!isLocalMode()) {
			final Region region = jBossCache.getRegion(regionFqn, true);
			if (region != null) {
				if (region.isActive()) {
					region.deactivate();
				}				
				region.unregisterContextClassLoader();
				jBossCache.removeRegion(regionFqn);
			}	
		}
	}*/
	
	/**
	 * Unsets the class loader to be used on serialization operations, for all
	 * data stored.
	 * @param classLoader
	 */
	/*public void unsetReplicationClassLoader(ClassLoader classLoader) {
		unsetReplicationClassLoader(Fqn.ROOT,classLoader);
	}*/
	
	/**
	 * Retrieves the cache content as a string.
	 * @return
	 */
	/*public String getCacheContent() {
		return "Mobicents Cache: " 
		+ "\n+-- Content:\n" + CachePrinter.printCacheDetails(jBossCache);
	}*/
}
