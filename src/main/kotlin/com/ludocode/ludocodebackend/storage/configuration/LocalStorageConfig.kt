package com.ludocode.ludocodebackend.storage.configuration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "storage.local")
@ConditionalOnProperty(prefix = "storage.gcs", name = ["enabled"], havingValue = "false")
@Component
data class LocalStorageConfig(
    var bucketName: String = "local-bucket"
)
