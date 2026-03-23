package com.ludocode.ludocodebackend.discussion.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionMessageResponse
import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessage
import org.springframework.stereotype.Component

@Component
class DiscussionMessageMapper(private val basicMapper: BasicMapper) {

    fun toDiscussionMessageResponseList(
        discussionMessages: List<DiscussionMessage>
    ) : List<DiscussionMessageResponse> =
            basicMapper.list(discussionMessages) { discussionMessage ->
                toDiscussionMessageResponse(discussionMessage)
            }


    fun toDiscussionMessageResponse(
        discussionMessage: DiscussionMessage
    ) : DiscussionMessageResponse {
        return DiscussionMessageResponse(
            id = discussionMessage.id,
            createdAt = discussionMessage.createdAt,
            discussionId = discussionMessage.discussionId,
            content = discussionMessage.content,
            authorId = discussionMessage.authorId,
            parentId = discussionMessage.parentId

        )
    }

}