package com.ludocode.ludocodebackend.discussion.api.dto

import com.ludocode.ludocodebackend.discussion.domain.enums.DiscussionTopic
import java.util.UUID

data class CreateDiscussionMessageRequest(
    val entityId: UUID,
    val discussionTopic: DiscussionTopic,
    val parentId: UUID?,
    val content: String
)
