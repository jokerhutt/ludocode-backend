package com.ludocode.ludocodebackend.subscription.app.service
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanFeatureResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanLimitsResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanOverviewResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.configuration.StripeProperties
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.subscription.domain.enum.SubscriptionLimit
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanFeatureRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanLimitRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
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
    private val stripeProperties: StripeProperties,
    private val subscriptionPlanLimitRepository: SubscriptionPlanLimitRepository,
    private val subscriptionPlanFeatureRepository: SubscriptionPlanFeatureRepository
) {
    private val logger = LoggerFactory.getLogger(SubscriptionService::class.java)


    fun getUserSubscriptionResponse(userId: UUID): UserSubscriptionResponse {

        val userPlan = userSubscriptionRepository.findByUserId(userId) ?: throw ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND)
        val subscriptionPlan = subscriptionPlanRepository.findByStripePriceId(userPlan.stripeSubscriptionId) ?: throw ApiException(
            ErrorCode.PLAN_NOT_FOUND)

        val limits = subscriptionPlanLimitRepository
            .findByPlanId(subscriptionPlan.id)
            .associateBy { it.limitCode }

        val maxProjects = limits[SubscriptionLimit.MAX_PROJECTS]?.limitValue ?: throw ApiException(ErrorCode.LIMITS_NOT_FOUND)
        val monthlyCredits = limits[SubscriptionLimit.AI_MONTHLY_CREDITS]?.limitValue ?: throw ApiException(ErrorCode.LIMITS_NOT_FOUND)

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

        val planIds = plans.map { it.id }

        val features = subscriptionPlanFeatureRepository.findAllByPlanIdIn(planIds)
        val limits   = subscriptionPlanLimitRepository.findAllByPlanIdIn(planIds)

        val featuresByPlan = features.groupBy { it.planId }
        val limitsByPlan   = limits.groupBy { it.planId }

        return plans.map { plan ->

            val planFeatures = featuresByPlan[plan.id].orEmpty()
                .map {
                    SubscriptionPlanFeatureResponse(
                        title = it.title,
                        enabled = it.enabled
                    )
                }

            val planLimits = limitsByPlan[plan.id].orEmpty()
                .map {
                    SubscriptionPlanLimitsResponse(
                        title = it.title,
                        limit = it.limitValue
                    )
                }

            SubscriptionPlanOverviewResponse(
                tier = plan.planCode,
                price = plan.displayPrice,
                period = plan.billingInterval,
                description = plan.description,
                recommended = plan.planCode == Plan.CORE,
                features = planFeatures,
                limits = planLimits
            )
        }
    }

    @Transactional
    fun activateSubscription(
        userId: UUID,
        planId: UUID,
        stripePriceId: String,
        stripeSubscriptionId: String,
        currentPeriodStart: OffsetDateTime,
        currentPeriodEnd: OffsetDateTime
    ) {

        logger.info("Activating subscription {}", kv("userId", userId), kv("planId", planId), kv("stripeSubscriptionId", stripeSubscriptionId))

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