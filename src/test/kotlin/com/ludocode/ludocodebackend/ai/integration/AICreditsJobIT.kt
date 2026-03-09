package com.ludocode.ludocodebackend.ai.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.api.dto.snapshot.StripeSubscriptionSnapshot
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionCommandPort
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlan
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestClocks.FIXED_AMS
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.util.SubscriptionPlanTestUtil
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.Test


class AICreditsJobIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var subscriptionService: SubscriptionService

    @Autowired
    private lateinit var stripeSubscriptionCommandPort: StripeSubscriptionCommandPort


    @Test
    fun subscriptionCancelled_switchToFree_CreditRenewalDateIsFirstOfNextMonth () {

        clock.set(FIXED_AMS.instant()) // 1st Jan 2025

        val plans = subscriptionPlanRepository.saveAll(SubscriptionPlanTestUtil.spawnDefaultSubscriptionPlans(clock))

        val supporterPlan = plans[1]

        val snapshot = StripeSubscriptionSnapshot(
            priceId = supporterPlan.stripePriceId,
            customerId = user1.stripeCustomerId!!,
            subscriptionId = "sub_1",
            periodStart = OffsetDateTime.now(clock),
            periodEnd = OffsetDateTime.now(clock).plusMonths(1),
            status = "active"
        )

        subscriptionService.upsertFromStripe(snapshot, false)

        val initialRes = submitGetCredits(user1.id)
        assertThat(initialRes).isEqualTo(PlanDefinitions.configFor(Plan.SUPPORTER).limits.monthlyAiCredits)

        clock.set(clock.instant().plus(1, ChronoUnit.DAYS))
        subscriptionService.handleSubscriptionDeleted(snapshot.subscriptionId)

        val res = submitGetCredits(user1.id)
        assertThat(res).isEqualTo(PlanDefinitions.configFor(Plan.FREE).limits.monthlyAiCredits)

        val subscriptionRes = submitGetSubscription(user1.id)
        val expected = OffsetDateTime
            .of(2025, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        assertThat(subscriptionRes.currentPeriodEnd)
            .isEqualTo(expected)

    }

    @Test
    fun subscriptionCancelled_resetsLimits() {

        clock.set(FIXED_AMS.instant()) // 1st Jan 2025

        val plans = subscriptionPlanRepository.saveAll(SubscriptionPlanTestUtil.spawnDefaultSubscriptionPlans(clock))

        val supporterPlan = plans[1]

        val snapshot = StripeSubscriptionSnapshot(
            priceId = supporterPlan.stripePriceId,
            customerId = user1.stripeCustomerId!!,
            subscriptionId = "sub_1",
            periodStart = OffsetDateTime.now(clock),
            periodEnd = OffsetDateTime.now(clock).plusDays(1),
            status = "active"
        )

        subscriptionService.upsertFromStripe(snapshot, false)

        val initialRes = submitGetCredits(user1.id)
        assertThat(initialRes).isEqualTo(PlanDefinitions.configFor(Plan.SUPPORTER).limits.monthlyAiCredits)

        clock.set(clock.instant().plus(1, ChronoUnit.DAYS))
        subscriptionService.handleSubscriptionDeleted(snapshot.subscriptionId)

        val res = submitGetCredits(user1.id)
        assertThat(res).isEqualTo(PlanDefinitions.configFor(Plan.FREE).limits.monthlyAiCredits)

    }

    private fun submitGetCredits(userId: UUID): Int =
        TestRestClient.getOk(ApiPaths.CREDITS.BASE, userId, Int::class.java)

    private fun submitGetSubscription(userId: UUID): UserSubscriptionResponse =
        TestRestClient.getOk(ApiPaths.SUBSCRIPTION.BASE, userId, UserSubscriptionResponse::class.java)



}