package com.ludocode.ludocodebackend.auth.app.port.out

import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import java.util.UUID

interface UserStreakPortForAuth {
    fun upsertStreak(userId: UUID) : UserStreakResponse
}