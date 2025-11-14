package com.ludocode.ludocodebackend.gcs.api.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.NoCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GcpConfig(
    @Value("\${app.gcs.host:}") private val gcsHost: String,
    @Value("\${gcs.project.id}") private val projectId: String
) {

    @Bean
    fun storage(): Storage {
        val builder = StorageOptions.newBuilder()
            .setProjectId(projectId)

        if (gcsHost.isNotBlank()) {
            builder.setHost(gcsHost)
            builder.setCredentials(NoCredentials.getInstance())
        } else {
            builder.setCredentials(GoogleCredentials.getApplicationDefault())
        }

        return builder.build().service
    }
}