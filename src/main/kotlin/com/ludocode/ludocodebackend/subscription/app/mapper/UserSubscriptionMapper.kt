package com.ludocode.ludocodebackend.subscription.app.mapper
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class UserSubscriptionMapper {

    fun toUserSubscriptionResponse(
        userId: UUID,
        planCode: Plan,
        cancelAtPeriodEnd: Boolean,
        currentPeriodEnd: OffsetDateTime?,
    ): UserSubscriptionResponse {

        val planDefinitions = PlanDefinitions.configFor(planCode)

        return UserSubscriptionResponse(
            userId = userId,
            planCode = planCode,
            monthlyCreditAllowance = planDefinitions.limits.monthlyAiCredits,
            maxProjects = planDefinitions.limits.maxProjects,
            currentPeriodEnd = currentPeriodEnd,
            cancelAtPeriodEnd = cancelAtPeriodEnd
        )
    }
}
