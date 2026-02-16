package com.ludocode.ludocodebackend.subscription.api.controller

import com.ludocode.ludocodebackend.commons.configuration.AppProps
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.request.CheckoutRequest
import com.ludocode.ludocodebackend.subscription.app.port.out.StripePort
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.subscription.configuration.StripeProperties
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.stripe.StripeClient
import com.stripe.model.Subscription
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.SUBSCRIPTION.BASE)
class SubscriptionController(
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val appProps: AppProps,
    private val stripeProperties: StripeProperties,
    private val stripePort: StripePort,
    private val subscriptionService: SubscriptionService
) {

    @PostMapping(ApiPaths.SUBSCRIPTION.CHECKOUT)
    fun createCheckoutSession(
        @AuthenticationPrincipal(expression = "userId") userId: UUID,
        @RequestBody request: CheckoutRequest
    ): Map<String, String> {

        val plan = subscriptionPlanRepository
            .findByPlanCodeAndIsActiveTrue(request.planCode)
            ?: throw ApiException(ErrorCode.PLAN_NOT_FOUND)

        val url = stripePort.createCheckoutSession(
            planPriceId = plan.stripePriceId,
            planId = plan.id,
            userId = userId
        )

        return mapOf("url" to url)
    }

    @PostMapping(ApiPaths.SUBSCRIPTION.WEBHOOK)
    fun handleWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") sigHeader: String
    ): ResponseEntity<Void> {

        val event = Webhook.constructEvent(
            payload,
            sigHeader,
            stripeProperties.webhookSecret
        )

        if (event.type == "checkout.session.completed") {

            val session = event.dataObjectDeserializer
                .getObject()
                .get() as Session

            val metadata = session.metadata
                ?: throw ApiException(ErrorCode.STRIPE_METADATA_MISSING)

            val userId = metadata["userId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            } ?: throw ApiException(ErrorCode.STRIPE_METADATA_MISSING)

            val planId = metadata["planId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            } ?: throw ApiException(ErrorCode.STRIPE_METADATA_MISSING)

            val stripeSubscriptionId = session.subscription as? String
                ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)

            val stripeSubscription = Subscription.retrieve(stripeSubscriptionId)

            val periodStart = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(stripeSubscription.items.data[0].currentPeriodStart),
                ZoneOffset.UTC
            )

            val periodEnd = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(stripeSubscription.items.data[0].currentPeriodEnd),
                ZoneOffset.UTC
            )

            subscriptionService.activateSubscription(
                userId,
                planId,
                stripeSubscriptionId,
                periodStart,
                periodEnd
            )
        }

        return ResponseEntity.ok().build()
    }



}