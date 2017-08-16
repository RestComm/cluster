/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.restcomm.cluster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.TransactionManager;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.jboss.logging.Logger;
import org.restcomm.cache.CacheDataExecutorService;
import org.restcomm.cache.MobicentsCache;
import org.restcomm.cluster.election.DefaultClusterElector;

public class MobicentsClusterFactory {

	private static final Logger log = Logger.getLogger(MobicentsClusterFactory.class);

	private String cacheConfig;
	private byte[] cacheConfigData;	
	private Configuration defaultConfig;
	private GlobalConfiguration globalConfig;
	private TransactionManager transactionManager;
	private DefaultClusterElector elector;
	private ClassLoader classLoader;
	private CacheContainer jBossCacheContainer;
	private CacheDataExecutorService cacheExecutorService;
    
	private ConcurrentHashMap<String, MobicentsCluster> clustersMap = new ConcurrentHashMap<String, MobicentsCluster>();

	public MobicentsClusterFactory(String cacheConfig, byte[] cacheConfigData, Configuration defaultConfig,
			GlobalConfiguration globalConfig, TransactionManager transactionManager, DefaultClusterElector elector,
			ClassLoader classLoader, CacheDataExecutorService cacheExecutorService) {
		this.cacheConfig = cacheConfig;
		this.cacheConfigData = cacheConfigData;
		this.defaultConfig = defaultConfig;
		this.globalConfig = globalConfig;
		this.transactionManager = transactionManager;
		this.elector = elector;
		this.classLoader = classLoader;
		this.cacheExecutorService = cacheExecutorService;
	}

	public MobicentsCluster getCluster(String name) {
		MobicentsCluster cluster = clustersMap.get(name);
		if (cluster == null) {
			MobicentsCache cache = initCache(name);
			cluster = new DefaultMobicentsCluster(cache, transactionManager, elector, cacheExecutorService);
			MobicentsCluster oldValue = clustersMap.putIfAbsent(name, cluster);
			if (oldValue != null)
				cluster = oldValue;
		}
		return cluster;
	}

	public void stopCluster(String name) {
		MobicentsCluster cluster = clustersMap.remove(name);
		if (cluster != null) {
			cluster.stopCluster();			
		}
	}

	public void stop() {
		Iterator<Entry<String, MobicentsCluster>> iterator=clustersMap.entrySet().iterator();
		while(iterator.hasNext())
		{
			Entry<String, MobicentsCluster> curr=iterator.next();
			MobicentsCluster cluster=curr.getValue();
			cluster.stopCluster();			
		}
		
		if(this.jBossCacheContainer!=null) {
			this.jBossCacheContainer.stop();
			this.jBossCacheContainer=null;
		}
		
		if(this.cacheExecutorService!=null) {
		    this.cacheExecutorService.terminate();
		    this.cacheExecutorService = null;
		}
	}
	
	private MobicentsCache initCache(String name) {		
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		MobicentsCache sleeCache = null;
		if(this.jBossCacheContainer!=null)
			sleeCache = new MobicentsCache(name,jBossCacheContainer, classLoader, cacheExecutorService);
		else {			
			Boolean loadDefault=false;
			try {
				if(cacheConfigData!=null) {
					InputStream cacheConfigStream =
							new ByteArrayInputStream(this.cacheConfigData);
	
					if(this.jBossCacheContainer==null)
						this.jBossCacheContainer=new DefaultCacheManager(cacheConfigStream, false);										
				}			
				else 
					loadDefault=true;
			} catch (IOException e) {
				log.warn("Cant create Mobicents Cache from config stream: " + this.cacheConfig, e);
				loadDefault=true;
			}
	
			if(loadDefault) {
				if(this.jBossCacheContainer==null)
					this.jBossCacheContainer=new DefaultCacheManager(globalConfig, defaultConfig, false);							
			}
			
			if(this.jBossCacheContainer!=null) {
				sleeCache = new MobicentsCache(name, jBossCacheContainer, classLoader, cacheExecutorService);
			}
		}
		
		Thread.currentThread().setContextClassLoader(currentClassLoader);
		return sleeCache;
	}
}
