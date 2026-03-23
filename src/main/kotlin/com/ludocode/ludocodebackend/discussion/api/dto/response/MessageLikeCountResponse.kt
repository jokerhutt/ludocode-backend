package com.ludocode.ludocodebackend.discussion.api.dto.response

import java.util.UUID

data class MessageLikeCountResponse(
    val id: UUID,
    val count: Int,
    val likedByMe: Boolean
)

