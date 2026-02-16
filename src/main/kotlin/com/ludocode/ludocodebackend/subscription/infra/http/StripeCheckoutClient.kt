package com.ludocode.ludocodebackend.subscription.infra.http

import com.ludocode.ludocodebackend.commons.configuration.AppProps
import com.ludocode.ludocodebackend.subscription.app.port.out.StripePort
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class StripeCheckoutClient (
    private val appProperties: AppProps
) : StripePort {

    override fun createCheckoutSession(planPriceId: String, planId: UUID, userId: UUID): String {

        val frontendUrl = appProperties.frontendUrl

        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSuccessUrl("$frontendUrl/billing/success")
            .setCancelUrl("$frontendUrl/billing/cancel")
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(planPriceId)
                    .setQuantity(1)
                    .build()
            )
            .putMetadata("userId", userId.toString())
            .putMetadata("planId", planId.toString())
            .build()

        val session = Session.create(params)
        return session.url!!
    }
}