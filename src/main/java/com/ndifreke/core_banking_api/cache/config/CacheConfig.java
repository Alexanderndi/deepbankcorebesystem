package com.ndifreke.core_banking_api.cache.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Objects;

@Configuration
@EnableCaching
public class CacheConfig {

    private CacheManager cacheManager;


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