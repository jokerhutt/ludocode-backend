package com.ludocode.ludocodebackend.playground.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("storage.gcs")
@Component
data class GcsFeatureConfig(
    var enabled: Boolean = true,
    var projectId: String = "",
    var host: String = "",
    var bucket: String = ""
)