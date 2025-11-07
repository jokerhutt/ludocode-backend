package com.ludocode.ludocodebackend.progress.app.port.out

import java.util.UUID

interface UserPortForProgress {
    fun getUserTimezone(userId: UUID) : String?
}