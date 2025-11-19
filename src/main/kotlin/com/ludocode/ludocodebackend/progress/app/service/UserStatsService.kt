package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.internal.StatsDelta
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStatsResponse
import com.ludocode.ludocodebackend.progress.app.mapper.UserStatsMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserStatsUseCase
import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import com.ludocode.ludocodebackend.progress.domain.enums.StreakAction
import com.ludocode.ludocodebackend.progress.infra.repository.UserStatsRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserStatsService(private val userStatsRepository: UserStatsRepository,
                       private val userStatsMapper: UserStatsMapper
) : UserStatsUseCase {

    fun getUserStatsList (userIds: List<UUID>) : List<UserStatsResponse> {
        return userStatsMapper.toUserStatsResponseList(userStatsRepository.findAllById(userIds))
    }

    @Transactional
    override fun findOrCreateStats (userId: UUID) : UserStatsResponse {
        val userStats = userStatsRepository.findById(userId).orElse(userStatsRepository.save(UserStats(userId, 0, 0)))
        return userStatsMapper.toUserStatsResponse(userStats)
    }

    @Transactional
    fun apply(delta: StatsDelta): UserStatsResponse {
        val stats = userStatsRepository.findById(delta.userId).orElseGet {
            userStatsRepository.save(UserStats(delta.userId, 0, 0))
        }
        stats.coins += delta.pointsDelta
        when (delta.streakAction) {
            StreakAction.INCREMENT -> stats.streak += 1
            StreakAction.RESET -> stats.streak = 0
            else -> {}
        }

        println("New Stats: Streak: ${stats.streak} Coins: ${stats.coins}")
        return userStatsMapper.toUserStatsResponse(userStatsRepository.save(stats))
    }

}