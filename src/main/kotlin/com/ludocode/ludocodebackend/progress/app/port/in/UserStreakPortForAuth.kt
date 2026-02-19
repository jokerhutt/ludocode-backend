package com.ludocode.ludocodebackend.progress.app.port.`in`

import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import java.util.*

interface UserStreakPortForAuth {
    fun getStreak(userId: UUID): UserStreakResponse
}