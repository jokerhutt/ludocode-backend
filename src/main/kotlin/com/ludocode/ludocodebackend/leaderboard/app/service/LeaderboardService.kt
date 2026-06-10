package com.ludocode.ludocodebackend.leaderboard.app.service

import com.ludocode.ludocodebackend.leaderboard.api.dto.LeaderboardUserResponse
import com.ludocode.ludocodebackend.leaderboard.api.dto.WeeklyLeaderboardResponse
import com.ludocode.ludocodebackend.progress.infra.repository.XpTransactionRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

@Service
class LeaderboardService (
    private val clock: Clock,
    private val xpTransactionRepository: XpTransactionRepository,
) {

    fun getWeeklyLeaderboardStats() : WeeklyLeaderboardResponse {

        val today = LocalDate.now(clock)
        val startDate = today.with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        )

        val endDate = startDate.plusDays(6)

        val startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC)
        val endDateTime = startDate.plusWeeks(1).atStartOfDay().atOffset(ZoneOffset.UTC)

        val weeklyLeaderboardUsers = xpTransactionRepository.findWeeklyLeaderboard(startDateTime, endDateTime)
            .mapIndexed { index, row ->
                LeaderboardUserResponse(
                    rank = index + 1, // They are ordered so like first in list is first place on lb
                    userId = row.getUserId(),
                    displayName = row.getDisplayName(),
                    avatarVersion = row.getAvatarVersion(),
                    avatarIndex = row.getAvatarIndex(),
                    xp = row.getXp()
                )
            }

        return WeeklyLeaderboardResponse(startDate, endDate, weeklyLeaderboardUsers)

    }


}