package com.ludocode.ludocodebackend.subscription.api.controller

import com.ludocode.ludocodebackend.commons.configuration.AppProps
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.subscription.api.dto.request.CheckoutRequest
import com.ludocode.ludocodebackend.subscription.app.port.out.StripePort
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.subscription.configuration.StripeProperties
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.stripe.StripeClient
import com.stripe.model.Subscription
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(SubscriptionController::class.java)


    @PostMapping(ApiPaths.SUBSCRIPTION.CHECKOUT)
    fun createCheckoutSession(
        @AuthenticationPrincipal(expression = "userId") userId: UUID,
        @RequestBody request: CheckoutRequest
    ): Map<String, String> {

        logger.info("Creating Stripe checkout session {}", kv("userId", userId), kv("planCode", request.planCode))

        val plan = subscriptionPlanRepository
            .findByPlanCodeAndIsActiveTrue(request.planCode)
            ?: run {
                logger.warn("Plan not found for checkout {}", kv("planCode", request.planCode))
                throw ApiException(ErrorCode.PLAN_NOT_FOUND)
            }

        val url = stripePort.createCheckoutSession(
            planPriceId = plan.stripePriceId,
            planId = plan.id,
            userId = userId
        )

        logger.info("Stripe checkout session created {}", kv("userId", userId), kv("checkoutUrl", url))

        return mapOf("url" to url)
    }

    @PostMapping(ApiPaths.SUBSCRIPTION.WEBHOOK)
    fun handleWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") sigHeader: String
    ): ResponseEntity<Void> {

        logger.info("Received Stripe webhook event")

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
                ?: run {
                    logger.warn("Stripe session metadata missing")
                    throw ApiException(ErrorCode.STRIPE_METADATA_MISSING)
                }

            val userId = metadata["userId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                ?: run {
                    logger.warn("Stripe metadata missing userId")
                    throw ApiException(ErrorCode.STRIPE_METADATA_MISSING)
                }

            val planId = metadata["planId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                ?: run {
                    logger.warn("Stripe metadata missing planId")
                    throw ApiException(ErrorCode.STRIPE_METADATA_MISSING)
                }

            val stripeSubscriptionId = session.subscription as? String
                ?: run {
                    logger.warn("Stripe subscription ID missing")
                    throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)
                }

            logger.info("Activating subscription for user", kv("userId", userId), kv("planId", planId), kv("stripeSubscriptionId", stripeSubscriptionId))

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

            logger.info("Subscription activated", kv("userId", userId), kv("planId", planId))
        } else {
            logger.debug("Ignoring Stripe event type {}", kv("eventType", event.type))
        }

        return ResponseEntity.ok().build()
    }



}