package com.ludocode.ludocodebackend.discussion.api.dto

import com.ludocode.ludocodebackend.discussion.domain.enums.DiscussionTopic
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateDiscussionMessageRequest(
    val entityId: UUID,
    val discussionTopic: DiscussionTopic,
    val parentId: UUID?,

    @field:NotBlank(message = "Content cannot be empty")
    @field:Size(max = 2000, message = "Content too long")
    val content: String
)
