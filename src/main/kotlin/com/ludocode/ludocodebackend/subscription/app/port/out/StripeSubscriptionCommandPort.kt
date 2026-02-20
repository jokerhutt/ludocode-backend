package com.ludocode.ludocodebackend.subscription.app.port.out

interface StripeSubscriptionCommandPort {
    fun cancelSubscription(subscriptionId: String)
    fun createSubscription(customerId: String, priceId: String): String
}