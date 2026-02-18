package com.ludocode.ludocodebackend.subscription.app.port.out

import java.util.UUID

interface SubscriptionPortForUser {
    fun cancelSubscription(userId: UUID)
}