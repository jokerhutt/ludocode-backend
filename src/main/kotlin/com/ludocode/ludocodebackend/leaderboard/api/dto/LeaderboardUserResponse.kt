package com.ludocode.ludocodebackend.leaderboard.api.dto

import java.util.UUID

data class LeaderboardUserResponse(
    val userId: UUID,
    val displayName: String?,
    val avatarVersion: String,
    val avatarIndex: Int,
    val rank: Int,
    val xp: Int
)