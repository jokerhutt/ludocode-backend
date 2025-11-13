package com.ludocode.ludocodebackend.gcs.api.config

import com.google.cloud.NoCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GcpConfig(
    @Value("\${app.gcs.host:}") private val gcsHost: String
) {

    @Bean
    fun storage(): Storage {
        val builder = StorageOptions.newBuilder()
            .setProjectId("ludo-test")
            .setCredentials(NoCredentials.getInstance())

        if (gcsHost.isNotBlank()) {
            builder.setHost(gcsHost)
        }

        return builder.build().service
    }
}