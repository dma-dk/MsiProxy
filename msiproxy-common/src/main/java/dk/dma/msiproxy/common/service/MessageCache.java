/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msiproxy.common.service;

import dk.dma.msiproxy.model.msi.Message;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.util.concurrent.IsolationLevel;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Implements the message cache with a default timeout of 10 minutes
 */
@Singleton
public class MessageCache {

    final static long LIFESPAN = 30 * 60 * 1000;   // 30 minutes
    final static int MAX_ENTRIES = 20000;          // at most 20.000 messages

    protected CacheContainer cacheContainer;

    @Inject
    private Logger log;

    /**
     * Returns a reference to the cache with the given cache key
     * @param cacheKey the cache key
     * @return a reference to the cache
     */
    public Cache<String, List<Message>> getCache(String cacheKey) {
        return cacheContainer.getCache(cacheKey);
    }

    /**
     * Should be called by sub-classes in a {@code @PostConstruct} method
     */
    @PostConstruct
    protected void init() {
        if (cacheContainer == null) {
            GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                    .nonClusteredDefault()  // Pre-configured for use in LOCAL mode
                    .build();               //Builds  the GlobalConfiguration object
            Configuration localConfiguration = new ConfigurationBuilder()
                    .clustering().cacheMode(CacheMode.LOCAL)
                    .locking().isolationLevel(IsolationLevel.REPEATABLE_READ)
                    .eviction().maxEntries(MAX_ENTRIES).strategy(EvictionStrategy.LRU)
                    .expiration().lifespan(LIFESPAN)
                    .build();
            cacheContainer = new DefaultCacheManager(globalConfiguration, localConfiguration, true);
            log.info("Created Infinispan message cache container");
        }
    }

    /**
     * Should be called by sub-classes in a {@code @PreDestroy} method
     */
    @PreDestroy
    public void destroy() {
        if (cacheContainer != null) {
            cacheContainer.stop();
            cacheContainer = null;
            log.info("Stopped Infinispan message cache container");
        }
    }
}
