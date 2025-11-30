package com.ludocode.ludocodebackend.progress.app.port.`in`

import com.ludocode.ludocodebackend.progress.dto.response.UserCoinsResponse
import java.util.UUID

interface UserCoinsPortForAuth {
    fun findOrCreateCoins(userId: UUID) : UserCoinsResponse
}