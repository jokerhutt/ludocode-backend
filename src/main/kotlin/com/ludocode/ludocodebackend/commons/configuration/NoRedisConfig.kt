package com.ludocode.ludocodebackend.commons.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    prefix = "spring.data.redis",
    name = ["enabled"],
    havingValue = "false",
    matchIfMissing = true
)
class NoRedisConfig {

    @Bean
    fun cacheManager(): CacheManager {
        return NoOpCacheManager()
    }
}