package com.ludocode.ludocodebackend.subscription.api.dto.response

import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.subscription.domain.enum.SubscriptionLimit
import java.math.BigDecimal

data class SubscriptionPlanOverviewResponse (
    val tier: Plan,
    val price: BigDecimal,
    val period: String,
    val description: String,
    val recommended: Boolean,
    val features: List<SubscriptionPlanFeatureResponse>,
    val limits: List<SubscriptionPlanLimitsResponse>
)

data class SubscriptionPlanFeatureResponse (
    val title: String,
    val enabled: Boolean
)

data class SubscriptionPlanLimitsResponse (
    val title: String,
    val limit: Int,
)