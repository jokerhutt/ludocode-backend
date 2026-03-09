package com.ludocode.ludocodebackend.subscription.app.mapper
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class UserSubscriptionMapper (
    private val clock: Clock
) {

    private fun nextMonthFirstDayUtc(): OffsetDateTime {
        val nowUtc = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC)

        return nowUtc
            .withDayOfMonth(1)
            .plusMonths(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
    }

    fun toUserSubscriptionResponse(
        userId: UUID,
        planCode: Plan,
        renewalDate: OffsetDateTime?,
        cancelAtPeriodEnd: Boolean,
    ): UserSubscriptionResponse {

        val planDefinitions = PlanDefinitions.configFor(planCode)

        val currentPeriodEnd =
            if (planCode == Plan.FREE || planCode == Plan.DEV)
                nextMonthFirstDayUtc()
            else
                renewalDate ?: throw ApiException(ErrorCode.PAID_PLAN_WITHOUT_RENEWAL)

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
