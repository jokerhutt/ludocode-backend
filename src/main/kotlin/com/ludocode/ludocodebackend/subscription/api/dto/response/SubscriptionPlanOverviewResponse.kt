package com.ludocode.ludocodebackend.subscription.api.dto.response

import com.ludocode.ludocodebackend.subscription.configuration.Feature
import com.ludocode.ludocodebackend.subscription.configuration.PlanLimits
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import java.math.BigDecimal

data class SubscriptionPlanOverviewResponse (
    val tier: Plan,
    val price: BigDecimal,
    val period: String,
    val description: String,
    val recommended: Boolean,
    val features: Set<Feature>,
    val limits: PlanLimits
)