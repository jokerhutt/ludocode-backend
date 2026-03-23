package com.ludocode.ludocodebackend.discussion.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionMessageResponse
import com.ludocode.ludocodebackend.discussion.api.dto.UserSummary
import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessage
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DiscussionMessageMapper(private val basicMapper: BasicMapper) {

    fun toDiscussionMessageResponseList(
        discussionMessages: List<DiscussionMessage>,
        users: Map<UUID, UserSummary>
    ): List<DiscussionMessageResponse> =
        basicMapper.list(discussionMessages) { msg ->
            toDiscussionMessageResponse(msg, users)
        }


    fun toDiscussionMessageResponse(
        discussionMessage: DiscussionMessage,
        users: Map<UUID, UserSummary>
    ): DiscussionMessageResponse {
        return DiscussionMessageResponse(
            id = discussionMessage.id,
            createdAt = discussionMessage.createdAt,
            discussionId = discussionMessage.discussionId,
            content = discussionMessage.content,
            authorId = discussionMessage.authorId,
            authorName = users[discussionMessage.authorId]?.username ?: "unknown",
            parentId = discussionMessage.parentId
        )
    }

    fun toDiscussionMessageResponse(
        discussionMessage: DiscussionMessage,
        user: UserSummary
    ): DiscussionMessageResponse {
        return DiscussionMessageResponse(
            id = discussionMessage.id,
            createdAt = discussionMessage.createdAt,
            discussionId = discussionMessage.discussionId,
            content = discussionMessage.content,
            authorId = discussionMessage.authorId,
            authorName = user.username,
            parentId = discussionMessage.parentId
        )
    }

}