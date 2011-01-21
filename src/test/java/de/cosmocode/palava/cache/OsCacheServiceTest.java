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

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

/**
 * A Test-Class for testing the {@link OsCacheService} class
 * which uses <a href="http://www.opensymphony.com/oscache/">OSCache</a>.
 *
 * @author Markus Baumann
 */
public final class OsCacheServiceTest extends CacheServiceTest {

    @Override
    protected long lifeTime() {
        return 2;
    }

    @Override
    protected long idleTime() {
        return 2;
    }

    @Override
    protected long sleepTimeBeforeIdleTimeout() {
        return 1;
    }

    @Override
    protected long sleepTimeUntilExpired() {
        return 4;
    }

    @Override
    protected TimeUnit timeUnit() {
        return TimeUnit.SECONDS;
    }
    
    @Override
    public CacheService unit() {
        final OsCacheService service = new OsCacheService();
        service.initialize();
        return service;
    }
    
    @Ignore
    @Override
    public void testStoreWithIdleTime() throws InterruptedException {
        
    }
    
}
