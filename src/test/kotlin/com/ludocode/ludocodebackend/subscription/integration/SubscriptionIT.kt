package com.ludocode.ludocodebackend.subscription.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.playground.api.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlan
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

class SubscriptionIT  : AbstractIntegrationTest() {

    @Test
    fun getSubscription_returnsSubscription () {

        val plan = subscriptionPlanRepository.save(SubscriptionPlan(
            id = UUID.randomUUID(),
            planCode = Plan.FREE,
            displayName = "Free",
            stripePriceId = "Random stripe price id",
            billingInterval = "month",
            aiMonthlyCreditlimit = 10,
            maxProjects = 1,
            prioritySupport = false,
            isActive = true,
            createdAt = OffsetDateTime.now(clock)
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
        assertThat(res.monthlyCreditAllowance).isEqualTo(plan.aiMonthlyCreditlimit)
        assertThat(res.maxProjects).isEqualTo(plan.maxProjects)



    }

    private fun submitGetSubscription (userId: UUID): UserSubscriptionResponse =
        TestRestClient.getOk(ApiPaths.SUBSCRIPTION.BASE, userId, UserSubscriptionResponse::class.java)


}