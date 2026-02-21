package com.ludocode.ludocodebackend.subscription.api.dto.response

import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import java.time.OffsetDateTime
import java.util.*

data class UserSubscriptionResponse(
    val userId: UUID,
    val planCode: Plan,
    val monthlyCreditAllowance: Int,
    val maxProjects: Int,
    val cancelAtPeriodEnd: Boolean,
    val currentPeriodEnd: Long,
)