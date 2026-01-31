package com.ludocode.ludocodebackend.commons.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProps(
    var host: String = "",
    var port: Int = 6379,
    var password: String = ""
)