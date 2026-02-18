package com.ludocode.ludocodebackend.subscription.app.port.out

import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import java.util.UUID

interface SubscriptionPortForAuth {
    fun getOrElseInitializeFreeSubscription (userId: UUID) : UserSubscriptionResponse
}