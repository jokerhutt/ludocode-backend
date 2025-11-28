package com.ludocode.ludocodebackend.playground.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("gcs")
@Component
class ProjectFeatureConfig {
    var enabled: Boolean = true
}