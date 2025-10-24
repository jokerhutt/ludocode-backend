package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.internal.StatsDelta
import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import com.ludocode.ludocodebackend.progress.domain.enums.StreakAction
import com.ludocode.ludocodebackend.progress.infra.repository.UserStatsRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserStatsService(private val userStatsRepository: UserStatsRepository) {

    fun getUserStats (userId: UUID) : UserStats {
        return userStatsRepository.findById(userId).orElseThrow()
    }

    @Transactional
    fun apply(delta: StatsDelta): UserStats {
        val stats = userStatsRepository.findById(delta.userId).orElseGet {
            userStatsRepository.save(UserStats(delta.userId, 0, 0))
        }
        stats.coins += delta.pointsDelta
        when (delta.streakAction) {
            StreakAction.INCREMENT -> stats.streak += 1
            StreakAction.RESET -> stats.streak = 0
            else -> {}
        }
        return userStatsRepository.save(stats)
    }



    fun updateUserStats(userId: UUID, newPoints: Int, newStreak: Int) {

    }

}