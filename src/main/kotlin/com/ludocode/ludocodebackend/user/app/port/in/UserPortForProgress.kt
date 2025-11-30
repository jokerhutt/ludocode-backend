package com.ludocode.ludocodebackend.user.app.port.`in`

import java.util.UUID

interface UserPortForProgress {
    fun getUserTimezone(userId: UUID) : String?
}