package com.ludocode.ludocodebackend.config.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class MutableClock(private var instant: Instant) : Clock() {

    private val zone = ZoneOffset.UTC

    override fun withZone(zone: ZoneId?): Clock = this

    override fun getZone(): ZoneId = zone

    override fun instant(): Instant = instant

    fun set(newInstant: Instant) {
        instant = newInstant
    }
}