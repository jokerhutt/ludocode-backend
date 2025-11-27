package com.ludocode.ludocodebackend.playground.config

import com.ludocode.ludocodebackend.playground.infra.http.PistonClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class PistonClientConfig {

    @Bean
    fun pistonClient(
        @Value("\${piston.base}") baseUrl: String
    ): PistonClient {
        return PistonClient(baseUrl)
    }
}