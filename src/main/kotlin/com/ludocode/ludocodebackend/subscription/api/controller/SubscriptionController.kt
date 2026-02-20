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
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionPort
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeWebhookPort
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.subscription.configuration.StripeProperties
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import com.stripe.model.checkout.Session
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
    private val userRepository: UserRepository,
    private val stripeSubscriptionPort: StripeSubscriptionPort,
    private val stripeWebhookPort: StripeWebhookPort
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
        stripeWebhookPort.handle(payload, sigHeader)
        return ok().build()
    }

}