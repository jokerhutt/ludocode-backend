package com.ludocode.ludocodebackend.progress.app.port.`in`

import com.ludocode.ludocodebackend.progress.dto.response.UserStreakResponse
import java.util.UUID

interface UserStreakPortForAuth {
    fun getStreak(userId: UUID) : UserStreakResponse
}