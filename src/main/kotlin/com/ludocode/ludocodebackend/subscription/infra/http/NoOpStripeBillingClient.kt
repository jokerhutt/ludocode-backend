package com.ludocode.ludocodebackend.subscription.infra.http
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeBillingPort

class NoOpStripeBillingClient : StripeBillingPort {

    override fun createBillingPortalSession(
        customerId: String
    ): String {
        throw ApiException(ErrorCode.BAD_REQ, "Stripe is disabled")
    }
}