package com.ludocode.ludocodebackend.discussion.api.dto

import java.time.OffsetDateTime
import java.util.UUID

data class DiscussionMessageResponse(
    val id: UUID,
    val discussionId: UUID,
    val authorId: UUID,
    val parentId: UUID?,
    val createdAt: OffsetDateTime,
    val content: String,
)
