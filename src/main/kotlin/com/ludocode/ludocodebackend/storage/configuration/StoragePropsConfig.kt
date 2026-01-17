package com.ludocode.ludocodebackend.storage.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(StorageProperties::class)
@Configuration
class StoragePropsConfig