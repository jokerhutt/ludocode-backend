package com.ludocode.ludocodebackend.subscription.infra.http
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeCheckoutPort
import java.util.UUID

class NoOpStripeCheckoutClient : StripeCheckoutPort {

    override fun createCheckoutSession(
        planPriceId: String,
        planId: UUID,
        userId: UUID,
        stripeCustomerId: String
    ): String {
        throw ApiException(ErrorCode.BAD_REQ, "Stripe is disabled")
    }
}