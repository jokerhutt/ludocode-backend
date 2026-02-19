package com.ludocode.ludocodebackend.progress.app.port.`in`

import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import java.util.*

interface UserCoinsPortForAuth {
    fun findOrCreateCoins(userId: UUID): UserCoinsResponse
}