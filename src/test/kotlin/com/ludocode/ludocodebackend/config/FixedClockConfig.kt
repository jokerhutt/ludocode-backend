package com.ludocode.ludocodebackend.config

import com.ludocode.ludocodebackend.support.TestClocks
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock

@TestConfiguration
class FixedClockConfig {
    @Bean
    fun clock(): Clock = TestClocks.FIXED_NOON_UTC
}