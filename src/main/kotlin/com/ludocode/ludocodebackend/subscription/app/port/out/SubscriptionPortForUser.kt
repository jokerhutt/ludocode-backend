package com.ludocode.ludocodebackend.subscription.app.port.out

import java.util.*

interface SubscriptionPortForUser {
    fun cancelSubscription(userId: UUID)
}