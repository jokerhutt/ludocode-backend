package com.ludocode.ludocodebackend.progress.infra.projection

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

interface UserStreakRow {
    fun getUserId(): UUID
    fun getCurrentStreakDays(): Int?
    fun getBestStreakDays(): Int?
    fun getLastMetLocalDate(): LocalDate?
    fun getLastMetGoalUtc(): OffsetDateTime?
}