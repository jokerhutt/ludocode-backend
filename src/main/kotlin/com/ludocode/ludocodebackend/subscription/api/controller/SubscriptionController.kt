package com.ludocode.ludocodebackend.subscription.api.controller
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.request.CheckoutRequest
import com.ludocode.ludocodebackend.subscription.api.dto.request.ConfirmRequest
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanOverviewResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeBillingPort
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeCheckoutPort
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.subscription.configuration.StripeProperties
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import com.stripe.model.Invoice
import com.stripe.model.Subscription
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Tag(
    name = "Subscriptions",
    description = "Operations related to subscription plans, checkout, and Stripe integration"
)
@RestController
@RequestMapping(ApiPaths.SUBSCRIPTION.BASE)
class SubscriptionController(
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val stripeProperties: StripeProperties,
    private val stripeCheckoutPort: StripeCheckoutPort,
    private val stripeBillingPort: StripeBillingPort,
    private val subscriptionService: SubscriptionService,
    private val userSubscriptionRepository: UserSubscriptionRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(SubscriptionController::class.java)

    @Operation(
        summary = "Get current user subscription",
        description = """
        Returns the subscription details for the currently authenticated user.
        Includes plan information, status, and billing period dates.
        Requires a valid session cookie.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    fun getUserSubscription(@AuthenticationPrincipal(expression = "userId") userId: UUID): ResponseEntity<UserSubscriptionResponse> {
        return ResponseEntity.ok(subscriptionService.getUserSubscriptionResponse(userId))
    }

    @Operation(
        summary = "Get active subscription plans",
        description = """
        Returns all currently active subscription plans available for purchase.
        Includes plan metadata such as name, price, and billing interval.
        """
    )
    @GetMapping(ApiPaths.SUBSCRIPTION.PLANS)
    fun getPlans(): ResponseEntity<List<SubscriptionPlanOverviewResponse>> {
        return ResponseEntity.ok(subscriptionService.getActivePlanOverviews())
    }

    @Operation(
        summary = "Confirm Stripe subscription",
        description = """
        Confirms a completed Stripe Checkout session.
        
        Validates the session, verifies metadata integrity, retrieves the Stripe subscription,
        and activates the paid subscription in the system.
        
        Requires a valid session cookie.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping(ApiPaths.SUBSCRIPTION.CONFIRM)
    fun confirmSubscription(
        @AuthenticationPrincipal(expression = "userId") userId: UUID,
        @RequestBody request: ConfirmRequest
    ): ResponseEntity<UserSubscriptionResponse> {

        val session = Session.retrieve(request.sessionId)

        val metadataUserId = session.metadata["userId"]
            ?: throw ApiException(ErrorCode.USER_NOT_FOUND)

        if (metadataUserId != userId.toString()) {
            throw ApiException(ErrorCode.BAD_REQ, "Not same user")
        }

        val stripeSubscriptionId = session.subscription as? String
            ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)

        val stripeSubscription = Subscription.retrieve(stripeSubscriptionId)

        if (stripeSubscription.status != "active") {
            throw ApiException(ErrorCode.PLAN_NOT_ACTIVE)
        }

        val stripeCustomerId = stripeSubscription.customer
            ?: throw ApiException(ErrorCode.STRIPE_CUSTOMER_INVALID)

        val stripePriceId = stripeSubscription.items.data[0].price.id

        val periodStart = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(stripeSubscription.items.data[0].currentPeriodStart),
            ZoneOffset.UTC
        )

        val periodEnd = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(stripeSubscription.items.data[0].currentPeriodEnd),
            ZoneOffset.UTC
        )

        subscriptionService.activatePaidSubscription(
            userId,
            stripePriceId,
            stripeCustomerId,
            stripeSubscriptionId,
            periodStart,
            periodEnd
        )

        val response = subscriptionService.getUserSubscriptionResponse(userId)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Create Stripe checkout session",
        description = """
        Creates a Stripe Checkout session for the specified subscription plan.
        
        Returns a redirect URL that the frontend must use to redirect the user
        to Stripe's hosted checkout page.
        
        Requires a valid session cookie.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
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

        val url = stripeCheckoutPort.createCheckoutSession(
            planPriceId = plan.stripePriceId,
            planId = plan.id,
            userId = userId
        )

        logger.info("Stripe checkout session created {}", kv("userId", userId), kv("checkoutUrl", url))

        return mapOf("url" to url)
    }

    @PostMapping(ApiPaths.SUBSCRIPTION.MANAGE)
    fun createManageSession(
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): Map<String, String> {

        val subscription = userSubscriptionRepository
            .findByUserId(userId)
            ?: throw ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND)

        if (subscription.stripeSubscriptionId == null) {
            throw ApiException(ErrorCode.BAD_REQ, "Free plan can not manage subscriptions")
        }

        val stripeCustomerUser = userRepository.findById(subscription.userId)
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        val stripeCustomerId = stripeCustomerUser.stripeCustomerId
            ?: throw ApiException(ErrorCode.STRIPE_CUSTOMER_INVALID)

        val url = stripeBillingPort.createBillingPortalSession(stripeCustomerId)

        return mapOf("url" to url)
    }

    @Operation(
        summary = "Handle Stripe webhook",
        description = """
        Endpoint for receiving Stripe webhook events.
        
        Validates the Stripe signature and processes relevant events
        such as checkout.session.completed to activate subscriptions.
        
        This endpoint is intended for Stripe and should not be called directly by clients.
        """
    )
    @Transactional
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

        when (event.type) {

            "invoice.paid" -> {

                val invoice = event.dataObjectDeserializer
                    .getObject()
                    .orElse(null) as? Invoice
                    ?: return ok().build()

                val stripeSubscriptionId = invoice.parent
                    ?.subscriptionDetails
                    ?.subscription
                    ?: return ok().build()

                val local = userSubscriptionRepository
                    .findByStripeSubscriptionId(stripeSubscriptionId)
                    ?: return ok().build()

                local.status = "ACTIVE"
                local.updatedAt = OffsetDateTime.now()

                subscriptionService.setAiCredits(
                    local.userId,
                    local.plan.planCode
                )
            }

            "customer.subscription.deleted" -> {

                val stripeSub = event.dataObjectDeserializer
                    .getObject()
                    .get() as Subscription

                val local = userSubscriptionRepository
                    .findByStripeSubscriptionId(stripeSub.id)
                    ?: return ok().build()

                subscriptionService.downgradeToFree(local.userId)

                logger.info(
                    "Subscription cancelled and downgraded to FREE",
                    kv("userId", local.userId),
                    kv("subscriptionId", stripeSub.id)
                )
            }

            "customer.subscription.updated" -> {

                val stripeSub = event.dataObjectDeserializer
                    .getObject()
                    .orElse(null) as? Subscription
                    ?: return ok().build()

                val local = userSubscriptionRepository
                    .findByStripeSubscriptionId(stripeSub.id)
                    ?: return ok().build()

                val isScheduledToCancel =
                    stripeSub.cancelAtPeriodEnd || stripeSub.cancelAt != null

                logger.error(
                    "STRIPE EVENT → subId={}, stripe.cancelAtPeriodEnd={}, stripe.cancelAt={}, computedShouldCancel={}",
                    stripeSub.id,
                    stripeSub.cancelAtPeriodEnd,
                    stripeSub.cancelAt,
                    isScheduledToCancel
                )

                local.cancelAtPeriodEnd = isScheduledToCancel

                logger.error(
                    "DB BEFORE COMMIT → subId={}, local.cancelAtPeriodEnd={}",
                    stripeSub.id,
                    local.cancelAtPeriodEnd
                )

                if (stripeSub.status == "active") {
                    val item = stripeSub.items.data.firstOrNull()
                        ?: return ok().build()

                    val periodStart = OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(item.currentPeriodStart),
                        ZoneOffset.UTC
                    )

                    val periodEnd = OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(item.currentPeriodEnd),
                        ZoneOffset.UTC
                    )

                    local.currentPeriodStart = periodStart
                    local.currentPeriodEnd = periodEnd
                }

                local.updatedAt = OffsetDateTime.now()

                userSubscriptionRepository.save(local)

                logger.warn(
                    """
                        UPDATED EVENT:
                          stripeSub.id={}
                          stripe.cancelAtPeriodEnd={}
                          stripe.cancelAt={}
                          computedShouldCancel={}
                          local.cancelAtPeriodEnd(afterSet)={}
                        """.trimIndent(),
                    stripeSub.id,
                    stripeSub.cancelAtPeriodEnd,
                    stripeSub.cancelAt,
                    isScheduledToCancel,
                    local.cancelAtPeriodEnd
                )

            }

            "checkout.session.completed" -> {
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

                logger.info(
                    "Activating subscription for user",
                    kv("userId", userId),
                    kv("planId", planId),
                    kv("stripeSubscriptionId", stripeSubscriptionId)
                )

                val stripeSubscription = Subscription.retrieve(stripeSubscriptionId)

                val stripeCustomerId = stripeSubscription.customer
                    ?: throw ApiException(ErrorCode.STRIPE_CUSTOMER_INVALID)

                val stripePriceId = stripeSubscription.items.data[0].price.id

                val periodStart = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(stripeSubscription.items.data[0].currentPeriodStart),
                    ZoneOffset.UTC
                )

                val periodEnd = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(stripeSubscription.items.data[0].currentPeriodEnd),
                    ZoneOffset.UTC
                )

                subscriptionService.activatePaidSubscription(
                    userId,
                    stripePriceId,
                    stripeCustomerId,
                    stripeSubscriptionId,
                    periodStart,
                    periodEnd
                )

                logger.info("Subscription activated", kv("userId", userId), kv("planId", planId))
            }

            else -> {
                logger.debug("Ignoring Stripe event type {}", kv("eventType", event.type))
            }

        }

        return ResponseEntity.ok().build()
    }

}