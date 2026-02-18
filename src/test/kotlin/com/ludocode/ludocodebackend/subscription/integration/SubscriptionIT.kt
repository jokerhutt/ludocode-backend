package com.ludocode.ludocodebackend.subscription.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanOverviewResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.configuration.Feature
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlan
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

class SubscriptionIT  : AbstractIntegrationTest() {

    @Test
    fun getPlanOverviews_returnsOverviews() {
        val freePlan = subscriptionPlanRepository.save(
            SubscriptionPlan(
                id = UUID.randomUUID(),
                planCode = Plan.FREE,
                displayName = "Free",
                stripePriceId = "price_free",
                billingInterval = "month",
                isActive = true,
                createdAt = OffsetDateTime.now(clock),
                displayPrice = BigDecimal.ZERO,
                description = "Get started with the basics",
                currency = "EUR"
            )
        )

        val corePlan = subscriptionPlanRepository.save(
            SubscriptionPlan(
                id = UUID.randomUUID(),
                planCode = Plan.CORE,
                displayName = "Core",
                stripePriceId = "price_core",
                billingInterval = "month",
                isActive = true,
                createdAt = OffsetDateTime.now(clock),
                displayPrice = BigDecimal("5.99"),
                description = "Unlock publishing and AI features",
                currency = "EUR"
            )
        )

        val res = submitGetPlans()

        assertThat(res).hasSize(2)

        val free = res.first { it.tier == Plan.FREE }
        val core = res.first { it.tier == Plan.CORE }

        assertThat(free.price).isEqualByComparingTo(BigDecimal.ZERO)
        assertThat(free.period).isEqualTo("month")
        assertThat(free.recommended).isFalse()
        assertThat(free.description).isEqualTo("Get started with the basics")

        assertThat(free.limits.maxProjects).isEqualTo(PlanDefinitions.configFor(Plan.FREE).limits.maxProjects)
        assertThat(free.limits.monthlyAiCredits).isEqualTo(PlanDefinitions.configFor(Plan.FREE).limits.monthlyAiCredits)

        assertThat(free.features).doesNotContain(Feature.SKILL_PATHS)

        assertThat(core.price).isEqualByComparingTo(corePlan.displayPrice)
        assertThat(core.recommended).isTrue()

        assertThat(core.limits.monthlyAiCredits).isEqualTo(PlanDefinitions.configFor(Plan.CORE).limits.monthlyAiCredits)

        assertThat(core.features).contains(Feature.CORE_COURSES)

    }

    @Test
    fun getSubscription_returnsSubscription () {

        val plan = subscriptionPlanRepository.save(SubscriptionPlan(
            id = UUID.randomUUID(),
            planCode = Plan.FREE,
            displayName = "Free",
            stripePriceId = "Random stripe price id",
            billingInterval = "month",
            isActive = true,
            createdAt = OffsetDateTime.now(clock),
            displayPrice = BigDecimal(0),
            description = "Get started with the basics",
            currency = "EUR"
        ))

        val userSubscription = userSubscriptionRepository.save(
            UserSubscription(
                id = UUID.randomUUID(),
                userId = user1.id,
                stripeSubscriptionId = plan.stripePriceId,
                status = "ACTIVE",
                currentPeriodStart = OffsetDateTime.now(clock),
                currentPeriodEnd = OffsetDateTime.now(clock).plusMonths(1),
                cancelAtPeriodEnd = false,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock),
                plan = plan
            )
        )

        val res = submitGetSubscription(user1.id)

        assertThat(res).isNotNull()

        assertThat(res.planCode).isEqualTo(Plan.FREE)
        assertThat(res.currentPeriodEnd).isEqualTo(userSubscription.currentPeriodEnd)
        assertThat(res.monthlyCreditAllowance).isEqualTo(PlanDefinitions.configFor(Plan.FREE).limits.monthlyAiCredits)
        assertThat(res.maxProjects).isEqualTo(PlanDefinitions.configFor(Plan.FREE).limits.maxProjects)



    }

    private fun submitGetSubscription (userId: UUID): UserSubscriptionResponse =
        TestRestClient.getOk(ApiPaths.SUBSCRIPTION.BASE, userId, UserSubscriptionResponse::class.java)

    private fun submitGetPlans(): List<SubscriptionPlanOverviewResponse> {
        val responseArray =
            TestRestClient.getOk(
                "${ApiPaths.SUBSCRIPTION.BASE}${ApiPaths.SUBSCRIPTION.PLANS}",
                user1.id,
                Array<SubscriptionPlanOverviewResponse>::class.java
            )

        return responseArray.toList()
    }


}