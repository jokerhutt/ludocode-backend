package com.ludocode.ludocodebackend.discussion.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.discussion.api.dto.response.MessageLikeCountResponse
import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessageLike
import com.ludocode.ludocodebackend.discussion.domain.entity.embeddable.DiscussionMessageLikeId
import com.ludocode.ludocodebackend.discussion.infra.repository.DiscussionMessageLikeRepository
import com.ludocode.ludocodebackend.discussion.infra.repository.DiscussionMessageRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DiscussionMessageLikeService(
    private val discussionMessageRepository: DiscussionMessageRepository,
    private val discussionMessageLikeRepository: DiscussionMessageLikeRepository,
) {

    @Transactional
    fun likeMessage(userId: UUID, messageId: UUID) {
        ensureMessageExists(messageId)

        val likeId = DiscussionMessageLikeId(userId, messageId)
        if (discussionMessageLikeRepository.existsById(likeId)) {
            return
        }

        discussionMessageLikeRepository.save(DiscussionMessageLike(likeId))
    }

    @Transactional
    fun unlikeMessage(userId: UUID, messageId: UUID) {
        ensureMessageExists(messageId)
        discussionMessageLikeRepository.deleteById(DiscussionMessageLikeId(userId, messageId))
    }

    fun getLikeCountByMessageId(userId: UUID, messageId: UUID): MessageLikeCountResponse {
        ensureMessageExists(messageId)

        val count = discussionMessageLikeRepository.countByMessageId(messageId).toInt()
        val likedByMe = discussionMessageLikeRepository.existsById(DiscussionMessageLikeId(userId, messageId))

        return MessageLikeCountResponse(
            id = messageId,
            count = count,
            likedByMe = likedByMe
        )
    }

    fun getLikeCountsByMessageIds(userId: UUID, messageIds: List<UUID>): List<MessageLikeCountResponse> {
        if (messageIds.isEmpty()) {
            return emptyList()
        }

        val existingMessageIds = discussionMessageRepository.findAllById(messageIds)
            .map { it.id }
            .toSet()

        if (existingMessageIds.size != messageIds.toSet().size) {
            throw ApiException(ErrorCode.ENTITY_NOT_FOUND)
        }

        val counts = discussionMessageLikeRepository.countByMessageIds(messageIds)
            .associate { it.getMessageId() to it.getLikeCount().toInt() }

        val likedSet = discussionMessageLikeRepository
            .findMessageIdsLikedByUser(userId, messageIds)
            .toSet()

        return messageIds.map { id ->
            MessageLikeCountResponse(
                id = id,
                count = counts[id] ?: 0,
                likedByMe = id in likedSet
            )
        }
    }

    private fun ensureMessageExists(messageId: UUID) {
        if (!discussionMessageRepository.existsById(messageId)) {
            throw ApiException(ErrorCode.ENTITY_NOT_FOUND)
        }
    }
}

