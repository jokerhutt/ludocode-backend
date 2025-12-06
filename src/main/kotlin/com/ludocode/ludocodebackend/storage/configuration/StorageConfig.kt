package com.ludocode.ludocodebackend.storage.configuration

import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.playground.config.GcsFeatureConfig
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForPlayground
import com.ludocode.ludocodebackend.storage.app.service.GcsStorageService
import com.ludocode.ludocodebackend.storage.app.service.LocalStorageService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageConfig(
    private val gcsConfig: GcsFeatureConfig,
    private val localConfig: LocalStorageConfig
) {

    @Bean
    @ConditionalOnProperty(prefix = "storage.gcs", name = ["enabled"], havingValue = "true")
    fun gcsStorageService(storage: Storage): StoragePortForPlayground {
        return GcsStorageService(storage, gcsConfig)
    }

    @Bean
    @ConditionalOnProperty(prefix = "storage.gcs", name = ["enabled"], havingValue = "false", matchIfMissing = true)
    fun localStorageService(): StoragePortForPlayground {
        return LocalStorageService(localConfig)
    }
}