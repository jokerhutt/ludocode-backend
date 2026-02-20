package com.ludocode.ludocodebackend.subscription.app.mapper

import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlan
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import org.springframework.stereotype.Component

@Component
class UserSubscriptionMapper {

    fun toUserSubscriptionResponse(
        userSubscription: UserSubscription,
        subscriptionPlan: SubscriptionPlan
    ): UserSubscriptionResponse {

        val planDefinitions = PlanDefinitions.configFor(subscriptionPlan.planCode)

        return UserSubscriptionResponse(
            userId = userSubscription.userId,
            planId = subscriptionPlan.id,
            planCode = subscriptionPlan.planCode,
            monthlyCreditAllowance = planDefinitions.limits.monthlyAiCredits,
            maxProjects = planDefinitions.limits.maxProjects,
            currentPeriodEnd = userSubscription.currentPeriodEnd,
            cancelAtPeriodEnd = userSubscription.cancelAtPeriodEnd
        )
    }
}
