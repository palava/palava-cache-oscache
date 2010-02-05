/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.cosmocode.palava.services.cache;

import java.io.Serializable;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.base.algorithm.FIFOCache;
import com.opensymphony.oscache.base.algorithm.LRUCache;
import com.opensymphony.oscache.base.algorithm.UnlimitedCache;

import de.cosmocode.palava.core.lifecycle.Initializable;

/**
 * An implementation of the {@link CacheService} interface
 * which uses <a href="http://www.opensymphony.com/oscache/">OSCache</a>.
 *
 * @author Markus Baumann
 */
public class OsCacheService implements CacheService, Initializable {
    
    /**
     * CacheClass to make completeUpdate() and clear() visible for the OsCacheService.
     * 
     * @author Markus Baumann
     */
    private static class NewCache extends Cache {
        
        private static final long serialVersionUID = -8607752508098786L;

        public NewCache(boolean useMemoryCaching, boolean unlimitedDiskCache,
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
    
    private NewCache cache;
    
    private boolean useMemoryCaching = true;
    
    private boolean unlimitedDiskCache;
    
    private boolean overflowPersistence = true;
    
    private boolean blocking;
    
    private String algorithmClass;
    
    private int capacity;
    
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
        this.algorithmClass = of(algorithmClass);
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
        cache = new NewCache(useMemoryCaching, unlimitedDiskCache,
            overflowPersistence, blocking, algorithmClass, capacity);
    }
    
    @Override
    public void store(Serializable key, Object value) {
        Preconditions.checkNotNull(key, "Key");
        cache.putInCache(Integer.toString(key.hashCode()), value);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        try {
            return (T) cache.getFromCache(Integer.toString(key.hashCode()));
        } catch (NeedsRefreshException e) {
            cache.cancelUpdate(Integer.toString(key.hashCode()));
            return null;
        }
    }
    
    @Override
    public <T> T remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final T item = this.<T>read(key);
        cache.removeEntry(Integer.toString(key.hashCode()));
        return item;
    }
    
    @Override
    public void clear() {
        cache.clear();
    }
    
}
