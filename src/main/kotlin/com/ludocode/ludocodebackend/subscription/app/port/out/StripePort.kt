package com.ludocode.ludocodebackend.subscription.app.port.out

import java.util.*

interface StripePort {
    fun createCheckoutSession(
        planPriceId: String,
        planId: UUID,
        userId: UUID
    ): String

    fun createBillingPortalSession(customerId: String): String

}