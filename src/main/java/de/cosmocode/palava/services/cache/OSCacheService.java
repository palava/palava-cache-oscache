/**
 * palava - a java-php-bridge
 * Copyright (C) 2007  CosmoCode GmbH
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.base.algorithm.FIFOCache;
import com.opensymphony.oscache.base.algorithm.LRUCache;
import com.opensymphony.oscache.base.algorithm.UnlimitedCache;

import de.cosmocode.palava.ServiceInitializationException;
import de.cosmocode.palava.core.service.lifecycle.Initializable;

/**
 * An implementation of the {@link CacheService} interface
 * which uses <a href="http://www.opensymphony.com/oscache/">OSCache</a>.
 * 
 * @author Willi Schoenborn
 */
public class OSCacheService implements CacheService, Initializable {

    private static final Logger log = LoggerFactory.getLogger(OSCacheService.class);
    
    @Inject
    @Named("oscache.useMemoryCaching")
    private boolean useMemoryCaching;

    @Inject
    @Named("oscache.unlimitedDiskCache")
    private boolean unlimitedDiskCache;

    @Inject
    @Named("oscache.overflowPersistence")
    private boolean overflowPersistence;

    @Inject
    @Named("oscache.blocking")
    private boolean blocking;
    
    private Class<?> algorithm;
    
    @Inject
    @Named("oscache.capacity")
    private int capacity;
    
    private ClearableCache cache;
    
    /**
     * A simple wrapper to gain access to {Cache#clear()}.
     *
     * @author Willi Schoenborn
     */
    private static class ClearableCache extends Cache {

        private static final long serialVersionUID = -26413516960339168L;

        public ClearableCache(boolean useMemoryCaching, boolean unlimitedDiskCache, boolean overflowPersistence,
            boolean blocking, String algorithmClass, int capacity) {
            super(useMemoryCaching, unlimitedDiskCache, overflowPersistence, blocking, algorithmClass, capacity);
        }
        
        @Override
        public void clear() {
            super.clear();
        }
        
    }

    @Inject
    void setAlgorithm(@Named("oscache.cacheMode") CacheMode cacheMode) {
        this.algorithm = of(cacheMode);
    }
    
    private Class<?> of(CacheMode mode) {
        switch (mode) {
            case LRU: {
                return LRUCache.class;
            }
            case FIFO: {
                return FIFOCache.class;
            }
            case UNLIMITED: {
                return UnlimitedCache.class;
            }
            default: {
                throw new UnsupportedOperationException(mode.name());
            }
        }
    }
    
    @Override
    public void initialize() throws ServiceInitializationException {
        log.info("OSCache [overflowPersistence=%s, unlimitedDiskCache=%s, useMemoryCaching=%s]", new Object[] {
            overflowPersistence, unlimitedDiskCache, useMemoryCaching
        });
        cache = new ClearableCache(
            useMemoryCaching,
            unlimitedDiskCache,
            overflowPersistence,
            blocking,
            algorithm.getName(),
            capacity
        );
    }
    
    @Override
    public void store(Serializable key, Object value) {
        cache.putInCache(uniqueString(key), value);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(Serializable key) {
        try {
            return (T) cache.getFromCache(uniqueString(key));
        } catch (NeedsRefreshException e) {
            return null;
        }
    }
    
    @Override
    public <T> T remove(Serializable key) {
        final T content = read(key);
        cache.removeEntry(uniqueString(key));
        return content;
    }
    
    @Override
    public void clear() {
        cache.clear();
    }
    
    // TODO test
    private String uniqueString(Serializable key) {
        return key.toString() + System.identityHashCode(key);
    }
    
}
