package com.ludocode.ludocodebackend.commons.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProps(
    val frontendUrl: String
)