package com.ludocode.ludocodebackend.config

import com.ludocode.ludocodebackend.runner.infra.http.PistonClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class PistonTestConfig {

    @Bean
    fun pistonClient(): PistonClient {
        return PistonClient("http://localhost:9999")
    }
}