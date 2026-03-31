package com.ludocode.ludocodebackend.commons.configuration.app

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AppProperties::class, MaintenanceProperties::class)
class AppConfig