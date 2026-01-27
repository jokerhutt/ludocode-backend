package com.ludocode.ludocodebackend.user.app.port.`in`

import java.util.UUID

interface UserPortForOnboarding {
    fun setDisplayName(userId: UUID, displayName: String)
}