package com.ludocode.ludocodebackend.gcs.api.config

import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GcpConfig {
    @Bean
    fun storage() = StorageOptions.getDefaultInstance().service
}