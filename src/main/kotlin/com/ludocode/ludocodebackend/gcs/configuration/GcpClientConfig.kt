package com.ludocode.ludocodebackend.gcs.configuration

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.NoCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.ludocode.ludocodebackend.playground.config.GcsFeatureConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@ConditionalOnProperty(prefix = "gcs", name = ["enabled"], havingValue = "true")
@Profile("!test")
class GcpClientConfig(
    private val gcs: GcsFeatureConfig
) {

    @Bean
    fun storage(): Storage {
        val builder = StorageOptions.newBuilder()
            .setProjectId(gcs.projectId)

        if (gcs.host.isNotBlank()) {
            builder.setHost(gcs.host)
            builder.setCredentials(NoCredentials.getInstance())
        } else {
            builder.setCredentials(GoogleCredentials.getApplicationDefault())
        }

        return builder.build().service
    }
}