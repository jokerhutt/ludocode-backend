package com.ludocode.ludocodebackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class HttpClientConfig {
    @Bean
    fun restTemplate() = RestTemplate()
}