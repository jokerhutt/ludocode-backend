package com.ludocode.ludocodebackend.playground.configuration

import com.ludocode.ludocodebackend.playground.infra.http.PistonClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class PistonClientConfig (private val pistonProperties: PistonProperties) {

    @Bean
    fun pistonClient(): PistonClient {
        val selected = pistonProperties.base?.takeIf { it.isNotBlank() } ?: pistonProperties.public
        return PistonClient(selected)
    }

}