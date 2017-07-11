package org.restcomm.cache;

import javax.transaction.Status;
import javax.transaction.SystemException;

import org.infinispan.manager.EmbeddedCacheManager;

public class CacheData<K, V> {
    private K key;
    private MobicentsCache cache;
    private CacheDataExecutorService cacheExecutorService;

    public CacheData(K key, MobicentsCache cache) {
        this.key = key;
        this.cache = cache;
        this.cacheExecutorService = cache.getCacheDataExecutorService();
    }

    public K getKey() {
        return this.key;
    }

    @SuppressWarnings("unchecked")
    protected V get() {
        V result = null;
        if (!isCurrentTransactionInRollback()) {
            result = (V) this.cache.getJBossCache().get(key);
        } else {
            result = cacheExecutorService.get(cache, key);
        }

        return result;
    }

    public Boolean exists() {
        boolean result = false;
        if (!isCurrentTransactionInRollback()) {
            result = this.cache.getJBossCache().containsKey(key);
        } else {
            Boolean exists = cacheExecutorService.exists(cache, key);
            if (exists != null) {
                result = exists;
            }
        }

        return result;
    }

    protected void put(V value) {
        // MAY BE USEFULL TO CONTROLL THE LOCKING FROM CODE IN FUTURE
        /*
         * try { if(this.cache.getJBossCache().getAdvancedCache().getTransactionManager().getTransaction()!=null) {
         * this.cache.getJBossCache().getAdvancedCache().lock(key); } } catch(SystemException ex) {
         * 
         * }
         */

        /*
         * Infinispan returns invalid state exception while expecting to do nothing on set there modifying the logic to simply
         * ignore rolledback transaction
         */
        
        if (!isCurrentTransactionInRollback())
            this.cache.getJBossCache().put(key, value);
    }

    @SuppressWarnings("unchecked")
    protected V remove() {
        // MAY BE USEFULL TO CONTROLL THE LOCKING FROM CODE IN FUTURE
        /*
         * try { if(this.cache.getJBossCache().getAdvancedCache().getTransactionManager().getTransaction()!=null) {
         * this.cache.getJBossCache().getAdvancedCache().lock(key); } } catch(SystemException ex) {
         * 
         * }
         */
        V result = null;
        if (!isCurrentTransactionInRollback())
            result = (V) this.cache.getJBossCache().remove(key);
        else
            result = get();

        return result;
    }

    private boolean isCurrentTransactionInRollback() {
        boolean result = false;
        try {
            int transactionStatus = cache.getJBossCache().getAdvancedCache().getTransactionManager().getStatus();
            if (transactionStatus == Status.STATUS_MARKED_ROLLBACK) {
                result = true;
            }
        } catch (SystemException ex) {
        }
        return result;
    }

    public EmbeddedCacheManager getCacheManager() {
        return this.cache.getJBossCache().getCacheManager();
    }

    public Boolean isLocal() {
        return this.cache.isLocalMode();
    }
    
    public void evict() {
        this.cache.getJBossCache().evict(key);
    }
}
