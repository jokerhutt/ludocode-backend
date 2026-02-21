package com.ludocode.ludocodebackend.subscription.app.port.out

import java.util.*

interface StripeCheckoutPort {
    fun createCheckoutSession(planPriceId: String, planId: UUID, userId: UUID, stripeCustomerId: String): String

}