package com.ludocode.ludocodebackend.user.configuration

import com.ludocode.ludocodebackend.commons.configuration.web.CorsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
data class AvatarConfig(
    var version: String = "v1",
    var count: Int = 10
)
