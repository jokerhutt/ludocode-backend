package com.ludocode.ludocodebackend.ai.app.service

import com.ludocode.ludocodebackend.ai.app.port.out.AiCreditPortForSubscription
import com.ludocode.ludocodebackend.ai.domain.entity.UserAICredits
import com.ludocode.ludocodebackend.ai.infra.repository.UserAICreditsRepository
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.configuration.StripeMode
import com.ludocode.ludocodebackend.subscription.configuration.StripeProperties
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class AICreditService(
    private val userAICreditsRepository: UserAICreditsRepository,
    private val stripeProperties: StripeProperties
): AiCreditPortForSubscription {

    private val logger = LoggerFactory.getLogger(AICreditService::class.java)

    @Transactional
    internal fun addCredits(userId: UUID, amount: Int): Int {
        if (amount <= 0) {
            throw ApiException(ErrorCode.BAD_REQ, "Amount must be a positive integer but received $amount")
        }
        return adjustCredits(userId, amount)
    }

    @Transactional
    internal fun deductCredits(userId: UUID): Int {
        return adjustCredits(userId, -1)
    }

    @Transactional
    internal fun deductCredits(userId: UUID, amountToDeduct: Int): Int {
        if (amountToDeduct <= 0) {
            throw ApiException(ErrorCode.BAD_REQ, "Amount must be a positive integer but received $amountToDeduct")
        }
        return adjustCredits(userId, (amountToDeduct * -1))
    }

    @Transactional
    override fun resetCredits(userId: UUID, amount: Int) {

        var userCreditsEntity = initializeOrGetCredits(userId)
        userCreditsEntity.credits = amount
    }

    @Transactional
    fun adjustCredits(userId: UUID, amount: Int): Int {

        var userCreditsEntity = initializeOrGetCredits(userId)
        val currentCredits = userCreditsEntity.credits
        val newCredits = currentCredits + amount

        if (amount < 0) {
            if (newCredits == 0) {
                logger.warn(
                    LogEvents.AI_CREDITS_EXHAUSTED + " {} {}",
                    kv(LogFields.USER_ID, userId.toString()),
                    kv(LogFields.OLD_CREDITS, currentCredits)
                )
            }

            if (newCredits < 0) {
                logger.warn(
                    LogEvents.AI_CREDITS_OVERDRAW_ATTEMPT + " {} {} {}",
                    kv(LogFields.USER_ID, userId.toString()),
                    kv(LogFields.OLD_CREDITS, currentCredits),
                    kv(LogFields.DELTA, amount)
                )
            }
        }

        if (newCredits < 0) {
            userCreditsEntity.credits = 0
        } else {
            userCreditsEntity.credits = newCredits
        }
        userAICreditsRepository.save(userCreditsEntity)
        logger.info(
            LogEvents.AI_CREDITS_ADJUSTED + " {} {} {} {}",
            kv(LogFields.USER_ID, userId.toString()),
            kv(LogFields.DELTA, amount),
            kv(LogFields.OLD_CREDITS, currentCredits),
            kv(LogFields.NEW_CREDITS, userCreditsEntity.credits)
        )
        return userCreditsEntity.credits
    }


    @Transactional
    internal fun initializeOrGetCredits(userId: UUID): UserAICredits {

        val existing = userAICreditsRepository.findById(userId).orElse(null)
        if (existing != null) return existing

        val stripeMode = stripeProperties.mode

        val initialPlan = when (stripeMode) {
            StripeMode.DEV_UNLIMITED -> Plan.DEV
            StripeMode.PROD -> Plan.FREE
            StripeMode.FREE_ONLY -> Plan.FREE
        }

        val allowance = PlanDefinitions.configFor(initialPlan).limits.monthlyAiCredits

        return try {
            val created = userAICreditsRepository.save(UserAICredits(userId, allowance))
            logger.info(LogEvents.AI_CREDITS_INITIALIZED + " {}", kv(LogFields.USER_ID, userId.toString()))
            created
        } catch (e: org.springframework.dao.DataIntegrityViolationException) {
            userAICreditsRepository.findById(userId).orElseThrow()
        }
    }

    @Transactional
    internal fun resetFreeCredits(userId: UUID) {

    }


}