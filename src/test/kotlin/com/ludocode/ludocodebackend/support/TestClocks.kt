package com.ludocode.ludocodebackend.support

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

object TestClocks {
    val FIXED_UTC =
        Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)

    val FIXED_NOON_UTC =
        Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneOffset.UTC)

    val FIXED_AMS =
        Clock.fixed(Instant.parse("2025-01-01T08:00:00Z"), ZoneId.of("Europe/Amsterdam"))

    val NEXT_DAY_UTC =
        Clock.fixed(Instant.parse("2025-01-02T00:00:00Z"), ZoneOffset.UTC)

    val FIXED_NOON_UTC_MONDAY =
        Clock.fixed(Instant.parse("2025-01-06T12:00:00Z"), ZoneOffset.UTC)

}