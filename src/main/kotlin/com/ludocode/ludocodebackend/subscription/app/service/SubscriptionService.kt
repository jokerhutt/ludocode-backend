package com.ludocode.ludocodebackend.subscription.app.service
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
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
    private val userSubscriptionRepository: UserSubscriptionRepository
) {
    private val logger = LoggerFactory.getLogger(SubscriptionService::class.java)


    @Transactional
    fun activateSubscription(
        userId: UUID,
        planId: UUID,
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

        val plan = subscriptionPlanRepository.findById(planId)
            .orElseThrow {
                logger.warn("Plan not found for subscription activation {}", kv("planId", planId))
                ApiException(ErrorCode.PLAN_NOT_FOUND)
            }


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