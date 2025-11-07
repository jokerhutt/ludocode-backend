package com.ludocode.ludocodebackend.support

import com.ludocode.ludocodebackend.support.TestClocks.FIXED_NOON_UTC
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@TestConfiguration
class FixedClockConfig {
    @Bean
    fun clock(): Clock = TestClocks.FIXED_NOON_UTC
}