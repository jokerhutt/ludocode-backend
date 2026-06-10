package com.ludocode.ludocodebackend.leaderboard.api.dto

import java.time.LocalDate
import java.time.OffsetDateTime

data class WeeklyLeaderboardResponse (
    val startDate: LocalDate,
    val endDate: LocalDate,
    val leaderboardUsers: List<LeaderboardUserResponse>
)