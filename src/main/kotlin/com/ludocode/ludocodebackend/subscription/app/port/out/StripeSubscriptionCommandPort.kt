package com.ludocode.ludocodebackend.subscription.app.port.out

interface StripeSubscriptionCommandPort {
    fun cancelSubscription(subscriptionId: String)
}