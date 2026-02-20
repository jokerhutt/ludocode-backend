package com.ludocode.ludocodebackend.subscription.app.port.out

import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import java.util.*

interface SubscriptionPortForAuth {
    fun ensureSubscriptionExists(userId: UUID)
}