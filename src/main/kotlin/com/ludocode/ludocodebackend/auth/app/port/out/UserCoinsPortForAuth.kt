package com.ludocode.ludocodebackend.auth.app.port.out

import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import java.util.UUID

interface UserCoinsPortForAuth {
    fun findOrCreateCoins(userId: UUID) : UserCoinsResponse
}