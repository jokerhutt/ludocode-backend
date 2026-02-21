package com.ludocode.ludocodebackend.subscription.app.service
import com.ludocode.ludocodebackend.ai.app.port.out.AiCreditPortForSubscription
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.response.SubscriptionPlanOverviewResponse
import com.ludocode.ludocodebackend.subscription.api.dto.response.UserSubscriptionResponse
import com.ludocode.ludocodebackend.subscription.api.dto.snapshot.StripeSubscriptionSnapshot
import com.ludocode.ludocodebackend.subscription.app.mapper.SubscriptionPlanOverviewMapper
import com.ludocode.ludocodebackend.subscription.app.mapper.UserSubscriptionMapper
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionCommandPort
import com.ludocode.ludocodebackend.subscription.app.port.out.SubscriptionPortForUser
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import net.logstash.logback.argument.StructuredArguments.kv
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val userSubscriptionRepository: UserSubscriptionRepository,
    private val userSubscriptionMapper: UserSubscriptionMapper,
    private val aiCreditPortForSubscription: AiCreditPortForSubscription,
    private val stripeSubscriptionCommandPort: StripeSubscriptionCommandPort,
    private val subscriptionPlanOverviewMapper: SubscriptionPlanOverviewMapper,

    ) : SubscriptionPortForUser {
    private val logger = LoggerFactory.getLogger(SubscriptionService::class.java)

    @Transactional
    fun handleSubscriptionDeleted(subscriptionId: String) {

        val local = userSubscriptionRepository
            .findByStripeSubscriptionId(subscriptionId)
            ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)

        local.status = "CANCELLED"
        local.cancelAtPeriodEnd = false
        local.updatedAt = OffsetDateTime.now()

        setAiCredits(local.userId, Plan.FREE)

    }

    fun isFreeUser(userId: UUID) : Boolean {
        return userSubscriptionRepository.findByUserIdAndStatusIn(userId, listOf("active", "trialing")) == null
    }

    @Transactional
    fun createCustomerId(userId: UUID) {

        val user = userRepository.findById(userId)
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }

        val userEmail = user.email

        if (userEmail == null) {
            throw ApiException(ErrorCode.EMAIL_NOT_FOUND)
        }

        val isExistingCustomer = user.stripeCustomerId != null

        val customerId : String = user.stripeCustomerId ?: run {
            val newCustomer = stripeSubscriptionCommandPort.createCustomer(
                email = userEmail,
                name = user.displayName
            )
            user.stripeCustomerId = newCustomer
            userRepository.save(user)
            newCustomer
        }

        if (!isExistingCustomer) {
            logger.info(
                LogEvents.STRIPE_CUSTOMER_CREATED + " {} {}",
                kv(LogFields.USER_ID, userId.toString()),
                kv(LogFields.STRIPE_CUSTOMER_ID, customerId)
            )
        }


    }

    @Transactional
    fun upsertFromStripe(snapshot: StripeSubscriptionSnapshot, cancelAtPeriodEnd: Boolean) {

        val user = userRepository
            .findByStripeCustomerId(snapshot.customerId)

        if (user == null) {
            logger.error(
                "Stripe subscription for unknown customer {}",
                snapshot.customerId
            )
            return
        }

        val plan = subscriptionPlanRepository
            .findByStripePriceId(snapshot.priceId)
            ?: throw ApiException(ErrorCode.PLAN_NOT_FOUND)

        val existing = userSubscriptionRepository
            .findByStripeSubscriptionId(snapshot.subscriptionId)

        if (existing != null) {

            val isNewBillingCycle =
                existing.currentPeriodStart != snapshot.periodStart

            existing.plan = plan
            existing.status = snapshot.status
            existing.currentPeriodStart = snapshot.periodStart
            existing.currentPeriodEnd = snapshot.periodEnd
            existing.cancelAtPeriodEnd = cancelAtPeriodEnd
            existing.updatedAt = OffsetDateTime.now()

            if (isNewBillingCycle && snapshot.status == "active") {
                setAiCredits(user.id, plan.planCode)
            }

        } else {

            userSubscriptionRepository.save(
                UserSubscription(
                    id = UUID.randomUUID(),
                    userId = user.id,
                    plan = plan,
                    stripeSubscriptionId = snapshot.subscriptionId,
                    status = snapshot.status,
                    currentPeriodStart = snapshot.periodStart,
                    currentPeriodEnd = snapshot.periodEnd,
                    cancelAtPeriodEnd = cancelAtPeriodEnd,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now()
                )
            )

            setAiCredits(user.id, plan.planCode)
        }

    }

    override fun cancelSubscription(userId: UUID) {

        val subscription = userSubscriptionRepository.findByUserId(userId)
            ?: throw ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND)

        val stripeId = subscription.stripeSubscriptionId
            ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)

        stripeSubscriptionCommandPort.cancelSubscription(stripeId)
    }

    fun getUserSubscriptionResponse(userId: UUID): UserSubscriptionResponse {
        val userPlan = userSubscriptionRepository.findByUserIdAndStatusIn(
            userId,
            listOf("active", "trialing")
        )

        val planCode = userPlan?.plan?.planCode ?: Plan.FREE
        val cancelAtPeriodEnd = userPlan?.cancelAtPeriodEnd ?: false
        val currentPeriodEnd = userPlan?.currentPeriodEnd ?: null
        val res = userSubscriptionMapper.toUserSubscriptionResponse(
            userId = userId,
            planCode = planCode,
            cancelAtPeriodEnd = cancelAtPeriodEnd,
            currentPeriodEnd = currentPeriodEnd,
            )
        return res
    }

    fun getActivePlanOverviews(): List<SubscriptionPlanOverviewResponse> {
        val plans = subscriptionPlanRepository.findAllByIsActiveTrue()
        if (plans.isEmpty()) return emptyList()
        return subscriptionPlanOverviewMapper.toPlanOverviewResponseList(plans)
    }

    private fun setAiCredits(userId: UUID, plan: Plan) {
        val creditLimits = PlanDefinitions.configFor(plan).limits.monthlyAiCredits
        aiCreditPortForSubscription.resetCredits(userId, creditLimits)
    }

}