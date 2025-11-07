package com.ludocode.ludocodebackend.support

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

object TestClocks {

    // Fixed UTC midnight
    val FIXED_UTC: Clock =
        Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)

    // Midday (useful if you want to simulate a "day already in progress")
    val FIXED_NOON_UTC: Clock =
        Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneOffset.UTC)

    // Local time (e.g. Europe/Amsterdam)
    val FIXED_AMS: Clock =
        Clock.fixed(Instant.parse("2025-01-01T08:00:00Z"), ZoneId.of("Europe/Amsterdam"))

    // “Next day” clock for streak or date-based testing
    val NEXT_DAY_UTC: Clock =
        Clock.fixed(Instant.parse("2025-01-02T00:00:00Z"), ZoneOffset.UTC)
}