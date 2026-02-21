package com.ludocode.ludocodebackend.subscription.configuration
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices
import com.ludocode.ludocodebackend.storage.app.service.LocalStorageService
import com.ludocode.ludocodebackend.storage.configuration.StorageProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SubscriptionConfig {

    @Bean
    @ConditionalOnProperty(prefix = "stripe", name = ["enabled"], havingValue = "true")
    fun localStorageService(props: StorageProperties): StoragePortForServices =
        LocalStorageService(props.local.bucketName)


}