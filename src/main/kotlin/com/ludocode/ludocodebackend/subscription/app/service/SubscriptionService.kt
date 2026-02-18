package com.ludocode.ludocodebackend.subscription.app.service
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanOverviewResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.app.port.out.SubscriptionPortForAuth
import com.ludocode.ludocodebackend.subscription.app.port.out.SubscriptionPortForUser
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.configuration.PlanLimits
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import com.stripe.model.Subscription
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val userSubscriptionRepository: UserSubscriptionRepository,

) : SubscriptionPortForAuth, SubscriptionPortForUser {
    private val logger = LoggerFactory.getLogger(SubscriptionService::class.java)

    fun getUserPlanLimits (userId: UUID) : PlanLimits {
        val userPlan = userSubscriptionRepository.findByUserId(userId)
            ?: throw ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND)
        val planDefinition = PlanDefinitions.configFor(userPlan.plan.planCode)
        return planDefinition.limits
    }

    fun getUserSubscriptionResponse(userId: UUID): UserSubscriptionResponse {

        val userPlan = userSubscriptionRepository.findByUserId(userId) ?: throw ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND)
        val subscriptionPlan = userPlan.plan

        val planDefinitions = PlanDefinitions.configFor(subscriptionPlan.planCode)

        val maxProjects = planDefinitions.limits.maxProjects
        val monthlyCredits = planDefinitions.limits.monthlyAiCredits

        val res = UserSubscriptionResponse(
            userId = userId,
            planId = subscriptionPlan.id,
            planCode = subscriptionPlan.planCode,
            monthlyCreditAllowance = monthlyCredits,
            maxProjects = maxProjects,
            currentPeriodEnd = userPlan.currentPeriodEnd
        )

        return res

    }

    fun getActivePlanOverviews(): List<SubscriptionPlanOverviewResponse> {

        val plans = subscriptionPlanRepository.findAllByIsActiveTrue()
        if (plans.isEmpty()) return emptyList()

        return plans.map { plan ->

            val config = PlanDefinitions.configFor(plan.planCode)

            SubscriptionPlanOverviewResponse(
                tier = plan.planCode,
                price = plan.displayPrice,
                period = plan.billingInterval,
                description = plan.description,
                recommended = config.recommended ?: false,
                features = config.features,
                limits = config.limits,

            )
        }
    }

    @Transactional
    override fun getOrElseInitializeFreeSubscription(
        userId: UUID
    ): UserSubscriptionResponse {

        val existing = userSubscriptionRepository.findByUserIdWithPlan(userId)

        if (existing != null) {
            return getUserSubscriptionResponse(userId)
        }

        val plan = subscriptionPlanRepository
            .findByPlanCodeAndIsActiveTrue(Plan.FREE)
            ?: throw ApiException(ErrorCode.PLAN_NOT_FOUND)

        val now = OffsetDateTime.now()
        val periodEnd = now.plusMonths(1)

        val subscription = UserSubscription(
            id = UUID.randomUUID(),
            userId = userId,
            plan = plan,
            stripeSubscriptionId = null,
            status = "ACTIVE",
            currentPeriodStart = now,
            currentPeriodEnd = periodEnd,
            cancelAtPeriodEnd = false,
            createdAt = now,
            updatedAt = now
        )

        userSubscriptionRepository.save(subscription)

        return getUserSubscriptionResponse(userId)
    }

    @Transactional
    override fun cancelSubscription(userId: UUID) {

        val subscription = userSubscriptionRepository.findByUserId(userId)
            ?: throw ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND)

        if (subscription.status != "ACTIVE") {
            return
        }

        val stripeSub = Subscription.retrieve(subscription.stripeSubscriptionId)
        stripeSub.cancel()

        subscription.status = "CANCELLED"
        subscription.cancelAtPeriodEnd = false
        subscription.currentPeriodEnd = OffsetDateTime.now()
        subscription.updatedAt = OffsetDateTime.now()
    }

    @Transactional
    fun activatePaidSubscription(
        userId: UUID,
        stripePriceId: String,
        stripeSubscriptionId: String,
        currentPeriodStart: OffsetDateTime,
        currentPeriodEnd: OffsetDateTime
    ) {

        logger.info("Activating subscription {}", kv("userId", userId), kv("stripeSubscriptionId", stripeSubscriptionId))

        userRepository.findById(userId)
            .orElseThrow {
                logger.warn("User not found for subscription activation {}", kv("userId", userId))
                ApiException(ErrorCode.USER_NOT_FOUND)
            }

        val plan = subscriptionPlanRepository.findByStripePriceId(stripePriceId)
            ?: throw ApiException(ErrorCode.PLAN_NOT_FOUND)


        val existing = userSubscriptionRepository.findByUserId(userId)

        if (existing != null) {
            existing.plan = plan
            existing.stripeSubscriptionId = stripeSubscriptionId
            existing.status = "ACTIVE"
            existing.currentPeriodStart = currentPeriodStart
            existing.currentPeriodEnd = currentPeriodEnd
            existing.cancelAtPeriodEnd = false
            existing.updatedAt = OffsetDateTime.now()

            logger.info("Updated existing subscription", kv("userId", userId), kv("subscriptionId", existing.id))

        } else {
            val subscription = UserSubscription(
                id = UUID.randomUUID(),
                userId = userId,
                plan = plan,
                stripeSubscriptionId = stripeSubscriptionId,
                status = "ACTIVE",
                currentPeriodStart = currentPeriodStart,
                currentPeriodEnd = currentPeriodEnd,
                cancelAtPeriodEnd = false,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )

            userSubscriptionRepository.save(subscription)
            logger.info("Created new subscription", kv("userId", userId), kv("subscriptionId", subscription.id))

        }
    }

}