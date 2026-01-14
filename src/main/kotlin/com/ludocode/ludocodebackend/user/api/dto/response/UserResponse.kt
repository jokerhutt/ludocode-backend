package com.ludocode.ludocodebackend.user.api.dto.response

import java.time.OffsetDateTime
import java.util.UUID

data class UserResponse (
    val id: UUID,
    val displayName: String?,
    val avatarVersion: String,
    val avatarIndex: Int,
    val email: String,
    val createdAt: OffsetDateTime,
    val hasOnboarded: Boolean
)