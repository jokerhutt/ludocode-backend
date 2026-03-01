package com.ludocode.ludocodebackend.commons.configuration.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    var origins: List<String> = listOf()
)