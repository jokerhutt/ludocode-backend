package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.config.time.MutableClock
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlan
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

object SubscriptionPlanTestUtil {

    fun spawnDefaultSubscriptionPlans (clock: MutableClock) : List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                id = UUID.randomUUID(),
                planCode = Plan.FREE,
                displayName = "Free",
                stripePriceId = "price_free",
                billingInterval = "month",
                isActive = true,
                createdAt = OffsetDateTime.now(clock).minusDays(2),
                displayPrice = BigDecimal.ZERO,
                description = "Get started with the basics",
                currency = "EUR"
            ),

            SubscriptionPlan(
                id = UUID.randomUUID(),
                planCode = Plan.SUPPORTER,
                displayName = "Core",
                stripePriceId = "price_core",
                billingInterval = "month",
                isActive = true,
                createdAt = OffsetDateTime.now(clock).minusDays(2),
                displayPrice = BigDecimal("5.99"),
                description = "Unlock publishing and AI features",
                currency = "EUR"
            )
        )
    }

}