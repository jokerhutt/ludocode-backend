package com.ludocode.ludocodebackend.storage.configuration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "storage.local")
@Component
data class LocalStorageConfig(
    var bucketName: String = "/data/local-bucket"
)
