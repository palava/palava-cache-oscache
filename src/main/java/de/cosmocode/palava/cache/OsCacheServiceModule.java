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

import java.lang.annotation.Annotation;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import de.cosmocode.palava.core.inject.AbstractRebindModule;
import de.cosmocode.palava.core.inject.Config;
import de.cosmocode.palava.core.inject.RebindModule;

/**
 * <p> Binds the {@link CacheService} to the {@link OsCacheService}.
 * </p>
 * <p> For a documentation of all configuration parameters take a look at the constructor
 * ({@link #OsCacheServiceModule()}).
 * </p>
 *
 * @author Willi Schoenborn
 * @author Oliver Lorenz
 */
public final class OsCacheServiceModule implements Module {
    
    /**
     * <h3> Binds the OsCache implementation to CacheService. </h3>
     * <h4> No necessary guice configuration parameters. </h4>
     * <h4> Optional guice configuration parameters: </h4>
     * <table>
     *   <tr>
     *   <th> oscache.useMemoryCaching (boolean) </th>
     *   <td> Specify if the memory caching is going to be used </td>
     *   </tr>
     *   <tr>
     *   <th> oscache.unlimitetdiskCache (boolean) </th>
     *   <td> Specify if the disk caching is unlimited </td>
     *   </tr>
     *   <tr>
     *   <th> oscache.overflowPersistence (boolean) </th>
     *   <td> Specify if the persistent cache is used in overflow only mode </td>
     *   </tr>
     *   <tr>
     *   <th> oscache.blocking (boolean) </th>
     *   <td> This parameter takes effect when a cache entry has just expired and
     *        several simultaneous requests try to retrieve it.
     *        While one request is rebuilding the content, the other requests will either block and
     *        wait for the new content (blocking == true) or instead receive a copy of the stale content
     *        so they don't have to wait (blocking == false).
     *        the default is false, which provides better performance
     *        but at the expense of slightly stale data being served.
     *   </td>
     *   </tr>
     *   <tr>
     *   <th> oscache.algorithmClass ({@link CacheMode}) </th>
     *   <td> The mode to use when the cache overflows and elements have to be removed from cache.
     *        Possible values:
     *        {@linkplain CacheMode#FIFO FIFO},
     *        {@linkplain CacheMode#LRU LRU},
     *        {@linkplain CacheMode#UNLIMITED UNLIMITED}
     *   </td>
     *   </tr>
     *   <tr>
     *   <th> oscache.capacity (int) </th>
     *   <td> The capacity </td>
     *   </tr>
     * </table>
     * 
     * @see #annotatedWith(Class, String)
     */
    public OsCacheServiceModule() {
        // constructor does nothing special, but is used for documentation
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(CacheService.class).to(OsCacheService.class).in(Singleton.class);
    }

    /**
     * <p> Rebinds all configuration entries using the specified prefix for configuration
     * keys and the supplied annotation for annotation rebindings.
     * </p>
     * <p> The config parameters must be given as <code> (prefix).oscache.(...) </code>
     * </p>
     * <p> Have a look at the {@linkplain #OsCacheServiceModule() constructor}
     * for a documentation of all configuration parameters.
     * </p>
     *
     * @param annotation the new binding annotation
     * @param prefix the prefix
     * @return a module which rebinds all required settings
     * 
     * @see #OsCacheServiceModule()
     */
    public static RebindModule annotatedWith(Class<? extends Annotation> annotation, String prefix) {
        Preconditions.checkNotNull(annotation, "Annotation");
        Preconditions.checkNotNull(prefix, "Prefix");
        return new AnnotatedInstanceModule(annotation, prefix);
    }

    /**
     * Internal {@link RebindModule} implementation.
     *
     * @since 2.0
     * @author Oliver Lorenz
     * @author Willi Schoenborn
     */
    private static final class AnnotatedInstanceModule extends AbstractRebindModule {

        private final Class<? extends Annotation> annotation;
        private final Config config;

        private AnnotatedInstanceModule(Class<? extends Annotation> annotation, String prefix) {
            this.annotation = annotation;
            this.config = new Config(prefix);
        }

        @Override
        protected void configuration() {
            // no mandatory configuration for oscache
        }

        @Override
        protected void optionals() {
            bind(boolean.class).annotatedWith(Names.named(OsCacheServiceConfig.USE_MEMORY_CACHING)).to(
                Key.get(boolean.class, Names.named(config.prefixed(OsCacheServiceConfig.USE_MEMORY_CACHING))));
            bind(boolean.class).annotatedWith(Names.named(OsCacheServiceConfig.UNLIMITED_DISK_CACHE)).to(
                    Key.get(boolean.class, Names.named(config.prefixed(OsCacheServiceConfig.UNLIMITED_DISK_CACHE))));
            bind(boolean.class).annotatedWith(Names.named(OsCacheServiceConfig.OVERFLOW_PERSISTENCE)).to(
                    Key.get(boolean.class, Names.named(config.prefixed(OsCacheServiceConfig.OVERFLOW_PERSISTENCE))));
            bind(boolean.class).annotatedWith(Names.named(OsCacheServiceConfig.BLOCKING)).to(
                    Key.get(boolean.class, Names.named(config.prefixed(OsCacheServiceConfig.BLOCKING))));
            bind(CacheMode.class).annotatedWith(Names.named(OsCacheServiceConfig.ALGORITHM_CLASS)).to(
                    Key.get(CacheMode.class, Names.named(config.prefixed(OsCacheServiceConfig.ALGORITHM_CLASS))));
            bind(int.class).annotatedWith(Names.named(OsCacheServiceConfig.CAPACITY)).to(
                    Key.get(int.class, Names.named(config.prefixed(OsCacheServiceConfig.CAPACITY))));
        }

        @Override
        protected void bindings() {
            bind(CacheService.class).annotatedWith(annotation).to(OsCacheService.class).in(Singleton.class);
        }

        @Override
        protected void expose() {
            expose(CacheService.class).annotatedWith(annotation);
        }
    }

}
