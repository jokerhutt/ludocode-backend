package com.ludocode.ludocodebackend.subscription.infra.http

import com.ludocode.ludocodebackend.commons.configuration.app.AppProperties
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeCheckoutPort
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import java.util.*

class StripeCheckoutClient(
    private val appProperties: AppProperties
) : StripeCheckoutPort {

    private val logger = LoggerFactory.getLogger(StripeCheckoutClient::class.java)

    override fun createCheckoutSession(planPriceId: String, planId: UUID, userId: UUID, stripeCustomerId: String): String {

        val frontendUrl = appProperties.frontendUrl

        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setCustomer(stripeCustomerId)
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
            LogEvents.STRIPE_CHECKOUT_SESSION_CREATED + " {} {} {}",
            kv(LogFields.USER_ID, userId.toString()),
            kv(LogFields.PLAN_ID, planId.toString()),
            kv(LogFields.STRIPE_SESSION_ID, session.id)
        )

        return session.url!!
    }
}