package com.ndifreke.core_banking_api.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

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
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            System.out.println("Cache Contents:");
            System.out.println(Objects.requireNonNull((cache.getNativeCache()).toString()));
        }else {
            System.out.println("No such cache found: ");

        }
    }
}