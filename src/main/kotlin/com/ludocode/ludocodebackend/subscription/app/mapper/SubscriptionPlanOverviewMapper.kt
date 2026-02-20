package com.ludocode.ludocodebackend.subscription.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanOverviewResponse
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlan
import org.springframework.stereotype.Component

@Component
class SubscriptionPlanOverviewMapper(private val basicMapper: BasicMapper) {
    fun toPlanOverviewResponse(plan: SubscriptionPlan): SubscriptionPlanOverviewResponse =
        basicMapper.one(plan) {
            val config = PlanDefinitions.configFor(it.planCode)

            SubscriptionPlanOverviewResponse(
                tier = it.planCode,
                price = it.displayPrice,
                period = it.billingInterval,
                description = it.description,
                recommended = config.recommended ?: false,
                features = config.features,
                limits = config.limits
            )
        }

    fun toPlanOverviewResponseList(
        plans: List<SubscriptionPlan>
    ): List<SubscriptionPlanOverviewResponse> =
        basicMapper.list(plans) { plan ->
            toPlanOverviewResponse(plan)
        }
}