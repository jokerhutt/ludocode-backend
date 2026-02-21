package com.ludocode.ludocodebackend.subscription.infra.http

import com.ludocode.ludocodebackend.commons.configuration.AppProps
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeCheckoutPort
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class StripeCheckoutClient(
    private val appProperties: AppProps
) : StripeCheckoutPort {

    private val logger = LoggerFactory.getLogger(StripeCheckoutPort::class.java)

    override fun createCheckoutSession(planPriceId: String, planId: UUID, userId: UUID, stripeCustomerId: String): String {

        val frontendUrl = appProperties.frontendUrl

        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setCustomer(stripeCustomerId)
            .setSubscriptionData()
            .setSuccessUrl("$frontendUrl/subscription/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl("$frontendUrl/subscription/cancel")
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

        logger.info(
            "Stripe checkout session created {}",
            kv("userId", userId.toString()),
            kv("planId", planId.toString()),
            kv("sessionId", session.id),
            kv("checkoutUrl", session.url)
        )

        return session.url!!
    }
}