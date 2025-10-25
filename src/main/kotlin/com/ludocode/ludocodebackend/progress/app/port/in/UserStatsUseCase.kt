package com.ludocode.ludocodebackend.progress.app.port.`in`

import com.ludocode.ludocodebackend.progress.api.dto.response.UserStatsResponse
import java.util.UUID

interface UserStatsUseCase {
    fun findOrCreateStats(userId: UUID) : UserStatsResponse
}