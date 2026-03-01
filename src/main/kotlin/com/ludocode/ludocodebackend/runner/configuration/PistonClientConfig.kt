package com.ludocode.ludocodebackend.runner.configuration

import com.ludocode.ludocodebackend.runner.configuration.PistonProperties
import com.ludocode.ludocodebackend.runner.infra.http.PistonClient
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