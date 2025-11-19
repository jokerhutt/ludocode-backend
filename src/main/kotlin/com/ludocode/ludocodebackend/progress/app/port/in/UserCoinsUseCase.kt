package com.ludocode.ludocodebackend.progress.app.port.`in`

import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import java.util.UUID

interface UserCoinsUseCase {
    fun findOrCreateCoins(userId: UUID) : UserCoinsResponse
}