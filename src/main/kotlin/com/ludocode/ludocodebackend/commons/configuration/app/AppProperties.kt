package com.ludocode.ludocodebackend.commons.configuration.app

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val frontendUrl: String,
)