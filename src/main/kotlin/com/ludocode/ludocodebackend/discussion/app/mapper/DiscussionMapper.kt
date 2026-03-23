package com.ludocode.ludocodebackend.discussion.app.mapper

import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionResponse
import com.ludocode.ludocodebackend.discussion.api.dto.UserSummary
import com.ludocode.ludocodebackend.discussion.domain.entity.Discussion
import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessage
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DiscussionMapper(private val discussionMessageMapper: DiscussionMessageMapper) {

    fun toDiscussionResponse(
        discussion: Discussion,
        messages: List<DiscussionMessage>,
        users: Map<UUID, UserSummary>
    ): DiscussionResponse {
        return DiscussionResponse(
            id = discussion.id,
            entityId = discussion.entityId,
            discussionTopic = discussion.discussionTopic,
            children = discussionMessageMapper.toDiscussionMessageResponseList(
                discussionMessages = messages,
                users = users
            )
        )
    }


}