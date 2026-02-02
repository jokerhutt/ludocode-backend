package com.ludocode.ludocodebackend.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.cache.CacheManager
import org.springframework.cache.support.NoOpCacheManager

@TestConfiguration
@EnableCaching
class TestCacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        return NoOpCacheManager()
    }
}
