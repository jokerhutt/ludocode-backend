package com.ludocode.ludocodebackend.subscription.app.service
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val userSubscriptionRepository: UserSubscriptionRepository
) {


    @Transactional
    fun activateSubscription(
        userId: UUID,
        planId: UUID,
        stripeSubscriptionId: String,
        currentPeriodStart: OffsetDateTime,
        currentPeriodEnd: OffsetDateTime
    ) {

        userRepository.findById(userId)
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }

        val plan = subscriptionPlanRepository.findById(planId)
            .orElseThrow { ApiException(ErrorCode.PLAN_NOT_FOUND) }

        val existing = userSubscriptionRepository.findByUserId(userId)

        if (existing != null) {
            existing.plan = plan
            existing.stripeSubscriptionId = stripeSubscriptionId
            existing.status = "ACTIVE"
            existing.currentPeriodStart = currentPeriodStart
            existing.currentPeriodEnd = currentPeriodEnd
            existing.cancelAtPeriodEnd = false
            existing.updatedAt = OffsetDateTime.now()
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
        }
    }

}