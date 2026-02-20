package com.ludocode.ludocodebackend.subscription.app.service
import com.ludocode.ludocodebackend.ai.app.port.out.AiCreditPortForSubscription
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanOverviewResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.api.dto.snapshot.StripeSubscriptionSnapshot
import com.ludocode.ludocodebackend.subscription.app.mapper.SubscriptionPlanOverviewMapper
import com.ludocode.ludocodebackend.subscription.app.mapper.UserSubscriptionMapper
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionCommandPort
import com.ludocode.ludocodebackend.subscription.app.port.out.SubscriptionPortForUser
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val userSubscriptionRepository: UserSubscriptionRepository,
    private val userSubscriptionMapper: UserSubscriptionMapper,
    private val aiCreditPortForSubscription: AiCreditPortForSubscription,
    private val stripeSubscriptionCommandPort: StripeSubscriptionCommandPort,
    private val subscriptionPlanOverviewMapper: SubscriptionPlanOverviewMapper,

    ) : SubscriptionPortForUser {
    private val logger = LoggerFactory.getLogger(SubscriptionService::class.java)

    @Transactional
    fun handleInvoicePaid(snapshot: StripeSubscriptionSnapshot) {

        val existing = userSubscriptionRepository
            .findByStripeSubscriptionId(snapshot.subscriptionId)

        val plan = subscriptionPlanRepository
            .findByStripePriceId(snapshot.priceId)
            ?: throw ApiException(ErrorCode.PLAN_NOT_FOUND)

        val now = OffsetDateTime.now()

        if (existing == null) {

            val userId = resolveUserFromCustomer(snapshot.customerId)

            val newSub = UserSubscription(
                id = UUID.randomUUID(),
                userId = userId,
                plan = plan,
                stripeSubscriptionId = snapshot.subscriptionId,
                status = "ACTIVE",
                currentPeriodStart = snapshot.periodStart,
                currentPeriodEnd = snapshot.periodEnd,
                cancelAtPeriodEnd = false,
                createdAt = now,
                updatedAt = now
            )

            userSubscriptionRepository.save(newSub)

        } else {

            existing.plan = plan
            existing.status = "ACTIVE"
            existing.currentPeriodStart = snapshot.periodStart
            existing.currentPeriodEnd = snapshot.periodEnd
            existing.cancelAtPeriodEnd = false
            existing.updatedAt = now
        }

        setAiCredits(
            resolveUserFromCustomer(snapshot.customerId),
            plan.planCode
        )
    }

    @Transactional
    fun handleSubscriptionUpdated(
        snapshot: StripeSubscriptionSnapshot,
        cancelAtPeriodEnd: Boolean,
        isActive: Boolean
    ) {
        val local = userSubscriptionRepository
            .findByStripeSubscriptionId(snapshot.subscriptionId)
            ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)

        local.cancelAtPeriodEnd = cancelAtPeriodEnd

        if (isActive) {
            local.currentPeriodStart = snapshot.periodStart
            local.currentPeriodEnd = snapshot.periodEnd
        }
    }

    @Transactional
    fun handleSubscriptionDeleted(subscriptionId: String) {

        val local = userSubscriptionRepository
            .findByStripeSubscriptionId(subscriptionId)
            ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)

        local.status = "CANCELLED"
        local.cancelAtPeriodEnd = false
        local.stripeSubscriptionId = null
        local.updatedAt = OffsetDateTime.now()
    }

    @Transactional
    fun ensureSubscriptionExists(userId: UUID) {

        val user = userRepository.findById(userId)
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }

        val userEmail = user.email

        if (userEmail == null) {
            throw ApiException(ErrorCode.EMAIL_NOT_FOUND)
        }

        val customerId : String = user.stripeCustomerId ?: run {
            val newCustomer = stripeSubscriptionCommandPort.createCustomer(
                email = userEmail,
                name = user.displayName
            )
            user.stripeCustomerId = newCustomer
            userRepository.save(user)
            newCustomer
        }

        val existing = userSubscriptionRepository.findByUserId(userId)
        if (existing?.stripeSubscriptionId != null) return

        val freePlan = subscriptionPlanRepository
            .findByPlanCodeAndIsActiveTrue(Plan.FREE)
            ?: throw ApiException(ErrorCode.PLAN_NOT_FOUND)

        stripeSubscriptionCommandPort.createSubscription(
            customerId = customerId,
            priceId = freePlan.stripePriceId
        )
    }

    @Transactional
    override fun cancelSubscription(userId: UUID) {

        val subscription = userSubscriptionRepository.findByUserId(userId)
            ?: throw ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND)

        if (subscription.status != "ACTIVE") {
            return
        }

        stripeSubscriptionCommandPort.cancelSubscription(
            subscription.stripeSubscriptionId
                ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)
        )

        subscription.status = "CANCELLED"
        subscription.cancelAtPeriodEnd = false
        subscription.currentPeriodEnd = OffsetDateTime.now()
        subscription.updatedAt = OffsetDateTime.now()
    }

    fun getUserSubscriptionResponse(userId: UUID): UserSubscriptionResponse {
        val userPlan = userSubscriptionRepository.findByUserId(userId) ?: throw ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND)
        val subscriptionPlan = userPlan.plan
        val res = userSubscriptionMapper.toUserSubscriptionResponse(userPlan, subscriptionPlan)
        return res
    }

    fun getActivePlanOverviews(): List<SubscriptionPlanOverviewResponse> {
        val plans = subscriptionPlanRepository.findAllByIsActiveTrue()
        if (plans.isEmpty()) return emptyList()
        return subscriptionPlanOverviewMapper.toPlanOverviewResponseList(plans)
    }

    private fun setAiCredits(userId: UUID, plan: Plan) {
        val creditLimits = PlanDefinitions.configFor(plan).limits.monthlyAiCredits
        aiCreditPortForSubscription.resetCredits(userId, creditLimits)
    }

    private fun resolveUserFromCustomer(customerId: String): UUID {
        val user = userRepository.findByStripeCustomerId(customerId)
            ?: throw ApiException(ErrorCode.USER_NOT_FOUND)

        return user.id
    }

}