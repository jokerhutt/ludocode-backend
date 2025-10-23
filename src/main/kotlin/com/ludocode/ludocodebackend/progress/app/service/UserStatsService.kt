package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import com.ludocode.ludocodebackend.progress.infra.repository.UserStatsRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserStatsService(private val userStatsRepository: UserStatsRepository) {

    fun getUserStats (userId: UUID) : UserStats {
        return userStatsRepository.findById(userId).orElseThrow()
    }



    fun updateUserStats(userId: UUID, newPoints: Int, newStreak: Int) {

    }

}