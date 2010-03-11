package de.cosmocode.palava.cache;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * Binds the {@link CacheService} to the {@link OsCacheService}.
 *
 * @author Willi Schoenborn
 */
public final class OsCacheServiceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(CacheService.class).to(OsCacheService.class).in(Singleton.class);
    }

}
