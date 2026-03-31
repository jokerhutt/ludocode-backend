package com.ludocode.ludocodebackend.commons.configuration.app

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "maintenance")
data class MaintenanceProperties(
    val enabled: Boolean = false
)

