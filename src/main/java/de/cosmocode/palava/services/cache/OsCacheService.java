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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;

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

        public NewCache(boolean useMemoryCaching, boolean unlimitedDiskCache, boolean overflowPersistence) {
            super(useMemoryCaching, unlimitedDiskCache, overflowPersistence);
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
    
    private static final Logger LOG = LoggerFactory.getLogger(OsCacheService.class);

    private NewCache cache;
    
    // TODO what does false mean here?
    private boolean useMemoryCaching = true;
    
    private boolean unlimitedDiskCache;
    
    private boolean overflowPersistence;
    
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
    
    @Override
    public void initialize() {
        LOG.info("Initialize OsCacheService.");
        cache = new NewCache(useMemoryCaching, unlimitedDiskCache, overflowPersistence);
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
            LOG.warn("Cache needs to be refreshed.", e);
            LOG.info("Call completeUpdate() for {}", key);
            cache.completeUpdate(Integer.toString(key.hashCode()));
            return (T) e.getCacheContent();
        }
    }
    
    @Override
    public <T> T remove(Serializable key) {
        final T item = read(key);
        cache.removeEntry(Integer.toString(key.hashCode()));
        return item;
    }
    
    @Override
    public void clear() {
        cache.clear();
    }
}
