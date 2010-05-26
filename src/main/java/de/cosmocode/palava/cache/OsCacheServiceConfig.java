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

/**
 * A config for {@link OsCacheService}.
 *
 * @since 2.0
 * @author Oliver Lorenz
 */
public final class OsCacheServiceConfig {
    
    public static final String PREFIX = CacheConfig.PREFIX + "oscache.";
    
    public static final String USE_MEMORY_CACHING = PREFIX + "useMemoryCaching";
    
    public static final String UNLIMITED_DISK_CACHE = PREFIX + "unlimitedDiskCache";
    
    public static final String OVERFLOW_PERSISTENCE = PREFIX + "overflowPersistence";
    
    public static final String BLOCKING = PREFIX + "blocking";
    
    public static final String ALGORITHM_CLASS = PREFIX + "algorithmClass";
    
    public static final String CAPACITY = PREFIX + "capacity";

    
    private OsCacheServiceConfig() {
        
    }

}
