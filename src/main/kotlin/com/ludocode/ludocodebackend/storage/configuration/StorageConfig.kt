package com.ludocode.ludocodebackend.storage.configuration

import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices
import com.ludocode.ludocodebackend.storage.app.service.GcsStorageService
import com.ludocode.ludocodebackend.storage.app.service.LocalStorageService
import com.ludocode.ludocodebackend.storage.app.service.S3StorageService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class StorageConfig {

    @Bean
    @ConditionalOnProperty(prefix = "storage", name = ["mode"], havingValue = "local")
    fun localStorageService(props: StorageProperties): StoragePortForServices =
        LocalStorageService(props.local.bucketName)

    @Bean
    @ConditionalOnProperty(prefix = "storage", name = ["mode"], havingValue = "gcs")
    fun gcsStorageService(gcsClient: Storage, props: StorageProperties): StoragePortForServices =
        GcsStorageService(gcsClient, props.gcs.bucket)

    @Bean
    @ConditionalOnProperty(prefix = "storage", name = ["mode"], havingValue = "s3")
    fun s3StorageService(s3Client: S3Client, props: StorageProperties): StoragePortForServices =
        S3StorageService(s3Client, props.s3.bucket)
}