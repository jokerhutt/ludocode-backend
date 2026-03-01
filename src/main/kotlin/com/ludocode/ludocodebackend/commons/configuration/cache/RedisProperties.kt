package com.ludocode.ludocodebackend.commons.configuration.cache

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperties(
    val enabled: Boolean = false,
    var host: String = "",
    var port: Int = 6379,
    var password: String = ""
)