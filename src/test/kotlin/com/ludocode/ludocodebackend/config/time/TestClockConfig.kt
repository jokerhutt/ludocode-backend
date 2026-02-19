package com.ludocode.ludocodebackend.config.time

import com.ludocode.ludocodebackend.support.TestClocks
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestClockConfig {

    @Bean
    fun clock(): MutableClock =
        MutableClock(TestClocks.FIXED_NOON_UTC.instant())
}