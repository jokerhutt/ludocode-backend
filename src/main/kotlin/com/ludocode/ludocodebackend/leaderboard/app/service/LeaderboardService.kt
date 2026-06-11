package com.ludocode.ludocodebackend.leaderboard.app.service

import com.ludocode.ludocodebackend.leaderboard.api.dto.LeaderboardUserResponse
import com.ludocode.ludocodebackend.leaderboard.api.dto.WeeklyLeaderboardResponse
import com.ludocode.ludocodebackend.progress.infra.repository.XpTransactionRepository
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForProgress
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import java.util.UUID

@Service
class LeaderboardService (
    private val clock: Clock,
    private val xpTransactionRepository: XpTransactionRepository,
    private val userPortForProgress: UserPortForProgress,
) {

    fun getWeeklyLeaderboardStats(userId: UUID) : WeeklyLeaderboardResponse {

        val today = LocalDate.now(clock)
        val startDate = today.with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        )

        val endDate = startDate.plusDays(6)

        val startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC)
        val endDateTime = startDate.plusWeeks(1).atStartOfDay().atOffset(ZoneOffset.UTC)

        if (!userQualifies(userId = userId, startDateTime, endDateTime)) {
            return WeeklyLeaderboardResponse(startDate, endDate, userQualifies = false, emptyList())
        }

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

        return WeeklyLeaderboardResponse(startDate, endDate, userQualifies = true, weeklyLeaderboardUsers)

    }

    // have they gotten at least some xp this week
    private fun userQualifies(userId: UUID, startDateTime: OffsetDateTime, endDateTime: OffsetDateTime) : Boolean {

        if (userPortForProgress.isGuestUser(userId)) {
            return false;
        }

        val hasXpThisWeek = xpTransactionRepository.existsByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, startDateTime, endDateTime)
        return hasXpThisWeek
    }


}