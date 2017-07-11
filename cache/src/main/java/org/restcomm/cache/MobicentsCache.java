/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
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

package org.restcomm.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.cache.impl.DecoratedCache;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.manager.CacheContainer;
import org.infinispan.remoting.transport.Address;

/**
 * The container's HA and FT data source.
 *
 * @author martins
 * @author András Kőkuti
 */
public class MobicentsCache {

    private static Logger logger = Logger.getLogger(MobicentsCache.class);

    private final CacheContainer jBossCacheContainer;

    private Cache<Object, Object> cache;

    private boolean localMode;
    private final boolean managedCache;
    private String name;

    private CacheDataExecutorService cacheDataExecutorService;

    private AtomicBoolean isStarted = new AtomicBoolean(false);

    public MobicentsCache(String name, CacheContainer jBossCacheContainer, ClassLoader classLoader,
            CacheDataExecutorService cacheDataExecutorService) {
        this.jBossCacheContainer = jBossCacheContainer;
        this.cacheDataExecutorService = cacheDataExecutorService;
        if (this.jBossCacheContainer.getCache().getCacheConfiguration().clustering().cacheMode().isClustered()) {
            this.cache = new DecoratedCache<Object, Object>(this.jBossCacheContainer.getCache(name).getAdvancedCache(),
                    classLoader);
        } else {
            this.cache = this.jBossCacheContainer.getCache(name);
        }

        this.name = name;
        this.managedCache = false;
        setLocalMode();
    }

    public MobicentsCache(String name, CacheContainer jBossCacheContainer, CacheDataExecutorService cacheDataExecutorService) {
        this.jBossCacheContainer = jBossCacheContainer;
        this.cacheDataExecutorService = cacheDataExecutorService;
        this.cache = this.jBossCacheContainer.getCache(name);
        this.managedCache = false;
        this.name = name;
        setLocalMode();
    }

    private void setLocalMode() {
        if (this.cache.getCacheConfiguration().clustering().cacheMode() == CacheMode.LOCAL) {
            localMode = true;
        }

    }

    public void startCache() {
        if (isStarted.compareAndSet(false, true)) {
            logger.info("Starting JBoss Cache " + name + " ...");
            this.cache.start();

            if (logger.isInfoEnabled()) {
                logger.info("Mobicents Cache  " + name + " started, status: " + cache.getStatus() + ", Mode: "
                        + cache.getCacheConfiguration().clustering().cacheMode());
            }
        }
    }

    public Address getLocalAddresss() {
        return cache.getCacheManager().getAddress();
    }

    protected CacheContainer getJBossCacheContainer() {
        return jBossCacheContainer;
    }

    protected Cache<Object, Object> getJBossCache() {
        return cache;
    }

    public boolean isBuddyReplicationEnabled() {
        // only for JBoss Cache based MobicentsCache
        return false;
    }

    public void setForceDataGravitation(boolean enableDataGravitation) {
        // only for JBoss Cache based MobicentsCache
    }

    public TransactionManager getTxManager() {
        return cache.getAdvancedCache().getTransactionManager();
    }

    public void stopCache() {
        if (!managedCache) {
            if (logger.isInfoEnabled()) {
                logger.info("Mobicents Cache " + name + " stopping...");
            }
            // this.jBossCacheManager.destroy();
        }
        if (logger.isInfoEnabled()) {
            logger.info("Mobicents Cache " + name + " stopped.");
        }
    }

    /**
     * Indicates if the cache is not in a cluster environment.
     *
     * @return the localMode
     */
    public boolean isLocalMode() {
        return localMode;
    }

    /*
     * Retreives all the values stored in specific cache beware of using this operation , its very very expensive
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Set getAllValues() {
        CloseableIterator<Object> values = getJBossCache().values().iterator();
        Set output = new HashSet();
        while (values.hasNext()) {
            output.add(values.next());
        }

        return output;
    }

    /*
     * Retreives all the keys stored in specific cache beware of using this operation , its very very expensive
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Set getAllKeys() {
        CloseableIterator<Object> values = getJBossCache().keySet().iterator();
        Set output = new HashSet();
        while (values.hasNext()) {
            output.add(values.next());
        }

        return output;
    }

    /*
     * Retreives all the keys stored in specific cache beware of using this operation , its very very expensive
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getAllElements() {
        CloseableIterator<Map.Entry<Object, Object>> values = getJBossCache().entrySet().iterator();
        Map output = new HashMap();
        while (values.hasNext()) {
            Map.Entry<Object, Object> curr = values.next();
            output.put(curr.getKey(), curr.getValue());
        }

        return output;
    }

    public void addListener(Object listener) {
        cache.addListener(listener);
    }

    public void addManagerListener(Object listener) {
        cache.getCacheManager().addListener(listener);
    }

    public List<Address> getCurrentView() {
        return new ArrayList<Address>(cache.getCacheManager().getMembers());
    }

    protected CacheDataExecutorService getCacheDataExecutorService() {
        return this.cacheDataExecutorService;
    }
}
