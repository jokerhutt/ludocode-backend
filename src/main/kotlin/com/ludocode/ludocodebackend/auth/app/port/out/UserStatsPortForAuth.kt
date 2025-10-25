package com.ludocode.ludocodebackend.auth.app.port.out

import com.ludocode.ludocodebackend.progress.api.dto.response.UserStatsResponse
import java.util.UUID

interface UserStatsPortForAuth {
    fun findOrCreateStats(userId: UUID) : UserStatsResponse
}