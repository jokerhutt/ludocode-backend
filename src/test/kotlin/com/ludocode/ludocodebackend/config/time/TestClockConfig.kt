package com.ludocode.ludocodebackend.config.time

import com.ludocode.ludocodebackend.support.TestClocks
import com.ludocode.ludocodebackend.support.TestClocks.FIXED_NOON_UTC
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

@TestConfiguration
class TestClockConfig {

    @Bean
    fun clock(): MutableClock =
        MutableClock(TestClocks.FIXED_NOON_UTC.instant())
}