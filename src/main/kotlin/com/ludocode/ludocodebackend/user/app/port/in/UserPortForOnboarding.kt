package com.ludocode.ludocodebackend.user.app.port.`in`

import java.util.*

interface UserPortForOnboarding {
    fun setDisplayName(userId: UUID, displayName: String)
}