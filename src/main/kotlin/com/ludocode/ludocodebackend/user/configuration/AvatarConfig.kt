package com.ludocode.ludocodebackend.user.configuration

import com.ludocode.ludocodebackend.commons.configuration.CorsProps
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CorsProps::class)
data class AvatarConfig(
    var version: String = "v1",
    var count: Int = 10
)
