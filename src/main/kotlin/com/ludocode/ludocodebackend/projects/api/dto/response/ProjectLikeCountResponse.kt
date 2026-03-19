package com.ludocode.ludocodebackend.projects.api.dto.response

import java.util.UUID

data class ProjectLikeCountResponse(
    val id: UUID,
    val count: Int,
    val likedByMe: Boolean
)

