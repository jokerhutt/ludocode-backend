package com.ludocode.ludocodebackend.gcs.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Clock

@Configuration
@Profile("!test")
class TimeConfig {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}