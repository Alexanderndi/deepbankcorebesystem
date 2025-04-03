package com.ndifreke.core_banking_api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;


/**
 * The type Cache config.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private CacheManager cacheManager;


    /**
     * Print cache contents.
     *
     * @param cacheName the cache name
     */
    public void printCacheContents(String cacheName) {
        if (cacheManager.getCache(cacheName) != null) {
            System.out.println(cacheManager.getCache(cacheName).getNativeCache());
        }
    }
}