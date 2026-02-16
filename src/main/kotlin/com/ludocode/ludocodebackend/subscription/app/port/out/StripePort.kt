package com.ludocode.ludocodebackend.subscription.app.port.out

import java.util.UUID

interface StripePort {
    fun createCheckoutSession(
        planPriceId: String,
        planId: UUID,
        userId: UUID
    ): String
}