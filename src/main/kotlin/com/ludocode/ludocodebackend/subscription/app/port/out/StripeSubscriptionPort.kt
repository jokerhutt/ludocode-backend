package com.ludocode.ludocodebackend.subscription.app.port.out

import com.ludocode.ludocodebackend.subscription.api.dto.snapshot.StripeSubscriptionSnapshot

interface StripeSubscriptionPort {
    fun retrieveSnapshot(subscriptionId: String): StripeSubscriptionSnapshot
}