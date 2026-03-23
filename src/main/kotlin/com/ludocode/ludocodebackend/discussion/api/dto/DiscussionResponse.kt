package com.ludocode.ludocodebackend.discussion.api.dto

import com.ludocode.ludocodebackend.discussion.domain.enums.DiscussionTopic
import java.util.UUID

data class DiscussionResponse(
    val id: UUID?,
    val entityId: UUID,
    val discussionTopic: DiscussionTopic,
    val children: List<DiscussionMessageResponse>
)
