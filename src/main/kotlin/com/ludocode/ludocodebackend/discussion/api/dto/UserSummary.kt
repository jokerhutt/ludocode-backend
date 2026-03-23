package com.ludocode.ludocodebackend.discussion.api.dto

import java.util.UUID

data class UserSummary(
    val id: UUID,
    val username: String
)
