package com.ludocode.ludocodebackend.progress.infra.projection

import java.util.UUID

interface LeaderboardRowProjection {
    fun getUserId(): UUID
    fun getDisplayName(): String?
    fun getAvatarVersion(): String
    fun getAvatarIndex(): Int
    fun getXp(): Int
}