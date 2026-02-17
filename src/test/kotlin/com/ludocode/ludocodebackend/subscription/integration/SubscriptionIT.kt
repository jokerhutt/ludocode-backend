package com.ludocode.ludocodebackend.subscription.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanOverviewResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlan
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlanFeature
import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlanLimit
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.subscription.domain.enum.SubscriptionFeature
import com.ludocode.ludocodebackend.subscription.domain.enum.SubscriptionLimit
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanFeatureRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanLimitRepository
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

class SubscriptionIT  : AbstractIntegrationTest() {

    @Autowired
    private lateinit var subscriptionPlanLimitRepository: SubscriptionPlanLimitRepository

    @Autowired
    private lateinit var subscriptionPlanFeatureRepository: SubscriptionPlanFeatureRepository

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

        subscriptionPlanFeatureRepository.saveAll(
            listOf(
                SubscriptionPlanFeature(UUID.randomUUID(), freePlan.id, "Access to core courses", SubscriptionFeature.CORE_COURSES, true),
                SubscriptionPlanFeature(UUID.randomUUID(), freePlan.id, "Code editor", SubscriptionFeature.CODE_EDITOR, true),
                SubscriptionPlanFeature(UUID.randomUUID(), freePlan.id, "Publish projects", SubscriptionFeature.PUBLISH_PROJECTS, false),
                SubscriptionPlanFeature(UUID.randomUUID(), freePlan.id, "Skill paths", SubscriptionFeature.SKILL_PATHS, false),
                SubscriptionPlanFeature(UUID.randomUUID(), freePlan.id, "AI Assistant", SubscriptionFeature.AI_ASSISTANT, false),
                SubscriptionPlanFeature(UUID.randomUUID(), freePlan.id, "Priority support", SubscriptionFeature.PRIORITY_SUPPORT, false)
            )
        )

        subscriptionPlanLimitRepository.saveAll(
            listOf(
                SubscriptionPlanLimit(UUID.randomUUID(), freePlan.id, "Max Projects", SubscriptionLimit.MAX_PROJECTS, 1),
                SubscriptionPlanLimit(UUID.randomUUID(), freePlan.id, "AI Monthly Credits", SubscriptionLimit.AI_MONTHLY_CREDITS, 10)
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

        subscriptionPlanFeatureRepository.saveAll(
            listOf(
                SubscriptionPlanFeature(UUID.randomUUID(), corePlan.id, "Access to core courses", SubscriptionFeature.CORE_COURSES, true),
                SubscriptionPlanFeature(UUID.randomUUID(), corePlan.id, "Code editor", SubscriptionFeature.CODE_EDITOR, true),
                SubscriptionPlanFeature(UUID.randomUUID(), corePlan.id, "Publish projects", SubscriptionFeature.PUBLISH_PROJECTS, true),
                SubscriptionPlanFeature(UUID.randomUUID(), corePlan.id, "Skill paths", SubscriptionFeature.SKILL_PATHS, true),
                SubscriptionPlanFeature(UUID.randomUUID(), corePlan.id, "AI Assistant", SubscriptionFeature.AI_ASSISTANT, true),
                SubscriptionPlanFeature(UUID.randomUUID(), corePlan.id, "Priority support", SubscriptionFeature.PRIORITY_SUPPORT, false)
            )
        )

        subscriptionPlanLimitRepository.saveAll(
            listOf(
                SubscriptionPlanLimit(UUID.randomUUID(), corePlan.id, "Max Projects", SubscriptionLimit.MAX_PROJECTS, 10),
                SubscriptionPlanLimit(UUID.randomUUID(), corePlan.id, "AI Monthly Credits", SubscriptionLimit.AI_MONTHLY_CREDITS, 200)
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

        assertThat(free.limits).anySatisfy {
            assertThat(it.title).isEqualTo("Max Projects")
            assertThat(it.limit).isEqualTo(1)
        }

        assertThat(free.features).anySatisfy {
            assertThat(it.title).isEqualTo("AI Assistant")
            assertThat(it.enabled).isFalse()
        }

        assertThat(core.price).isEqualByComparingTo(BigDecimal("5.99"))
        assertThat(core.recommended).isTrue()

        assertThat(core.limits).anySatisfy {
            assertThat(it.title).isEqualTo("AI Monthly Credits")
            assertThat(it.limit).isEqualTo(200)
        }

        assertThat(core.features).anySatisfy {
            assertThat(it.title).isEqualTo("Publish projects")
            assertThat(it.enabled).isTrue()
        }





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

        val planFeatures =
            mutableListOf(
                SubscriptionPlanFeature(
                    id = UUID.randomUUID(),
                    planId = plan.id,
                    title = "Access to core courses",
                    featureCode = SubscriptionFeature.CORE_COURSES,
                    enabled = true
                ),
                SubscriptionPlanFeature(
                    id = UUID.randomUUID(),
                    planId = plan.id,
                    title = "Code editor access",
                    featureCode = SubscriptionFeature.CODE_EDITOR,
                    enabled = true
                ),
                SubscriptionPlanFeature(
                    id = UUID.randomUUID(),
                    planId = plan.id,
                    title = "AI Assistant",
                    featureCode = SubscriptionFeature.AI_ASSISTANT,
                    enabled = false
                ),
                SubscriptionPlanFeature(
                    id = UUID.randomUUID(),
                    planId = plan.id,
                    title = "Priority support",
                    featureCode = SubscriptionFeature.PRIORITY_SUPPORT,
                    enabled = false
                )
            )

        subscriptionPlanFeatureRepository
            .saveAll<SubscriptionPlanFeature>(planFeatures)

        val planLimits = subscriptionPlanLimitRepository.saveAll(
            listOf(
                SubscriptionPlanLimit(
                    id = UUID.randomUUID(),
                    planId = plan.id,
                    title = "Max Projects",
                    limitCode = SubscriptionLimit.MAX_PROJECTS,
                    limitValue = 3
                ),
                SubscriptionPlanLimit(
                    id = UUID.randomUUID(),
                    planId = plan.id,
                    title = "AI Monthly Credits",
                    limitCode = SubscriptionLimit.AI_MONTHLY_CREDITS,
                    limitValue = 50
                )
            )
        )

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
        assertThat(res.monthlyCreditAllowance).isEqualTo(50)
        assertThat(res.maxProjects).isEqualTo(3)



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