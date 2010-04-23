/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.base.algorithm.FIFOCache;
import com.opensymphony.oscache.base.algorithm.LRUCache;
import com.opensymphony.oscache.base.algorithm.UnlimitedCache;
import com.opensymphony.oscache.web.filter.ExpiresRefreshPolicy;

import de.cosmocode.palava.core.lifecycle.Initializable;

/**
 * An implementation of the {@link CacheService} interface
 * which uses <a href="http://www.opensymphony.com/oscache/">OSCache</a>.
 *
 * @author Markus Baumann
 * @author Oliver Lorenz (maxAge)
 */
final class OsCacheService implements CacheService, Initializable {
    
    /**
     * CacheClass to make completeUpdate() and clear() visible for the OsCacheService.
     * 
     * @author Markus Baumann
     * @author Willi Schoenborn
     */
    private static class ClearableCache extends Cache {
        
        private static final long serialVersionUID = -8607752508098786L;

        public ClearableCache(boolean useMemoryCaching, boolean unlimitedDiskCache,
                boolean overflowPersistence, boolean blocking, String algorithmClass, int capacity) {
            super(useMemoryCaching, unlimitedDiskCache, overflowPersistence, blocking, algorithmClass, capacity);
        }
        
        @Override
        public void completeUpdate(String key) {
            super.completeUpdate(key);
        }
        
        @Override
        public void clear() {
            super.clear();
        }
        
    }
    
    private long defaultMaxAge = DEFAULT_MAX_AGE;
    
    private TimeUnit defaultMaxAgeUnit = DEFAULT_MAX_AGE_TIMEUNIT;
    
    
    private ClearableCache cache;
    
    private boolean useMemoryCaching = true;
    
    private boolean unlimitedDiskCache;
    
    private boolean overflowPersistence = true;
    
    private boolean blocking;
    
    private String algorithmClass = LRUCache.class.getName();
    
    private int capacity = -1;
    
    @Inject(optional = true)
    void setUseMemoryCaching(@Named("oscache.useMemoryCaching") boolean useMemoryCaching) {
        this.useMemoryCaching = useMemoryCaching;
    }
    
    @Inject(optional = true)
    void setUnlimitedDiskCache(@Named("oscache.unlimitedDiskCache") boolean unlimitedDiskCache) {
        this.unlimitedDiskCache = unlimitedDiskCache;
    }
    
    @Inject(optional = true)
    void setOverflowPersistence(@Named("oscache.overflowPersistence") boolean overflowPersistence) {
        this.overflowPersistence = overflowPersistence;
    }
    
    @Inject(optional = true)
    void setBlocking(@Named("oscache.blocking") boolean blocking) {
        this.blocking = blocking;
    }

    @Inject(optional = true)
    void setAlgorithmClass(@Named("oscache.algorithmClass") CacheMode algorithmClass) {
        this.algorithmClass = of(Preconditions.checkNotNull(algorithmClass, "AlgorithmClass"));
    }
    
    @Inject(optional = true)
    void setCapacity(@Named("oscache.capacity") int capacity) {
        this.capacity = capacity;
    }
    
    private String of(CacheMode mode) {
        switch (mode) {
            case LRU: {
                return LRUCache.class.getName();
            }
            case UNLIMITED: {
                return UnlimitedCache.class.getName();
            }
            case FIFO: {
                return FIFOCache.class.getName();
            }
            default: {
                throw new UnsupportedOperationException(mode.name());
            }
        }
    }
    
    @Override
    public void initialize() {
        cache = new ClearableCache(useMemoryCaching, unlimitedDiskCache,
            overflowPersistence, blocking, algorithmClass, capacity);
    }
    
    @Override
    public long getMaxAge() {
        return getMaxAge(TimeUnit.SECONDS);
    }
    
    @Override
    public long getMaxAge(TimeUnit unit) {
        return unit.convert(defaultMaxAge, defaultMaxAgeUnit);
    }
    
    @Override
    public void setMaxAge(long maxAgeSeconds) {
        this.setMaxAge(maxAgeSeconds, TimeUnit.SECONDS);
    }
    
    @Override
    public void setMaxAge(long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkArgument(maxAge >= 0, MAX_AGE_NEGATIVE);
        Preconditions.checkNotNull(maxAgeUnit, "MaxAge TimeUnit");
        
        this.defaultMaxAge = maxAge;
        this.defaultMaxAgeUnit = maxAgeUnit;
    }
    
    @Override
    public void store(Serializable key, Object value) {
        this.store(key, value, defaultMaxAge, defaultMaxAgeUnit);
    }
    
    @Override
    public void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit) {

        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkArgument(maxAge >= 0, MAX_AGE_NEGATIVE);
        Preconditions.checkNotNull(maxAgeUnit, "MaxAge TimeUnit");
        
        final int refreshPeriod;
        if (maxAge == DEFAULT_MAX_AGE && maxAgeUnit == DEFAULT_MAX_AGE_TIMEUNIT) {
            refreshPeriod = -1;
        } else {
            refreshPeriod = (int) maxAgeUnit.toSeconds(maxAge);
        }
        final EntryRefreshPolicy policy = new ExpiresRefreshPolicy(refreshPeriod);
        final String cacheKey = key + Integer.toString(key.hashCode());
        cache.putInCache(cacheKey, value, policy);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        try {
            return (T) cache.getFromCache(key + Integer.toString(key.hashCode()));
        } catch (NeedsRefreshException e) {
            cache.cancelUpdate(key + Integer.toString(key.hashCode()));
            return null;
        }
    }
    
    @Override
    public <T> T remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final T item = this.<T>read(key);
        cache.removeEntry(key + Integer.toString(key.hashCode()));
        return item;
    }
    
    @Override
    public void clear() {
        cache.clear();
    }
    
}
