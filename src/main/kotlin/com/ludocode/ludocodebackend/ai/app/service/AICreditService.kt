package com.ludocode.ludocodebackend.ai.app.service

import com.ludocode.ludocodebackend.ai.domain.entity.UserAICredits
import com.ludocode.ludocodebackend.ai.infra.repository.UserAICreditsRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@ConditionalOnProperty(prefix = "ai", name = ["enabled"], havingValue = "true")
@Service
class AICreditService(private val userAICreditsRepository: UserAICreditsRepository) {

    private val INITIAL_USER_CREDITS: Int = 10

    @Transactional
    internal fun addCredits (userId: UUID, amount: Int): Int {
        if (amount <= 0) {
            throw ApiException(ErrorCode.BAD_REQ, "Amount must be a positive integer but received $amount")
        }
        return adjustCredits(userId, amount)
    }

    @Transactional
    internal fun deductCredits (userId: UUID): Int {
        return adjustCredits(userId, -1)
    }

    @Transactional
    internal fun deductCredits (userId: UUID, amountToDeduct: Int): Int {
        if (amountToDeduct <= 0) {
            throw ApiException(ErrorCode.BAD_REQ, "Amount must be a positive integer but received $amountToDeduct")
        }
        return adjustCredits(userId, (amountToDeduct * -1))
    }

    private fun adjustCredits (userId: UUID, amount: Int): Int {
        var userCreditsEntity = initializeOrGetCredits(userId)
        val currentCredits = userCreditsEntity.credits
        val newCredits = currentCredits + amount
        if (newCredits < 0) {
            userCreditsEntity.credits = 0
        } else {
            userCreditsEntity.credits = newCredits
        }
        userAICreditsRepository.save(userCreditsEntity)
        return userCreditsEntity.credits
    }


    @Transactional
    internal fun initializeOrGetCredits (userId: UUID) : UserAICredits {
        return userAICreditsRepository.findById(userId).orElseGet { userAICreditsRepository.save(
            UserAICredits(userId, INITIAL_USER_CREDITS)
        ) }
    }


}