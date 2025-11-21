package com.ludocode.ludocodebackend.ai.app.service

import com.ludocode.ludocodebackend.ai.domain.entity.UserAICredits
import com.ludocode.ludocodebackend.ai.infra.repository.UserAICreditsRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AICreditService(private val userAICreditsRepository: UserAICreditsRepository) {

    @Transactional
    internal fun handleDeductCredits (userId: UUID): Int {
        val creditsToDeduct = 1
        return deductCredits(userId, creditsToDeduct)
    }

    private fun deductCredits (userId: UUID, amountToDeduct: Int): Int {
        var userCreditsEntity = initializeOrGetCredits(userId)
        val currentCredits = userCreditsEntity.credits
        val newCredits = currentCredits - amountToDeduct
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
            UserAICredits(userId, 10)
        ) }
    }


}