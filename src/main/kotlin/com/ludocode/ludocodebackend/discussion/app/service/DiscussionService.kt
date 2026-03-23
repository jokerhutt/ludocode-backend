package com.ludocode.ludocodebackend.discussion.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.discussion.api.dto.CreateDiscussionMessageRequest
import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionMessageResponse
import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionResponse
import com.ludocode.ludocodebackend.discussion.api.dto.UserSummary
import com.ludocode.ludocodebackend.discussion.app.mapper.DiscussionMessageMapper
import com.ludocode.ludocodebackend.discussion.domain.entity.Discussion
import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessage
import com.ludocode.ludocodebackend.discussion.domain.enums.DiscussionTopic
import com.ludocode.ludocodebackend.discussion.infra.repository.DiscussionMessageRepository
import com.ludocode.ludocodebackend.discussion.infra.repository.DiscussionRepository
import com.ludocode.ludocodebackend.lesson.app.service.LessonService
import com.ludocode.ludocodebackend.projects.app.service.ProjectService
import com.ludocode.ludocodebackend.user.app.service.UserService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class DiscussionService(
    private val discussionRepository: DiscussionRepository,
    private val lessonService: LessonService,
    private val projectService: ProjectService,
    private val discussionMessageRepository: DiscussionMessageRepository,
    private val clock: Clock,
    private val discussionMessageMapper: DiscussionMessageMapper,
    private val userService: UserService,
) {

    private fun getOrCreateDiscussion(entityId: UUID, discussionTopic: DiscussionTopic) : Discussion {
        return discussionRepository
            .findByEntityIdAndDiscussionTopic(entityId, discussionTopic)
            ?: run {
                if (!validateEntityId(entityId, discussionTopic)) {
                    throw ApiException(ErrorCode.ENTITY_NOT_FOUND)
                }

                discussionRepository.save(
                    Discussion(
                        id = UUID.randomUUID(),
                        entityId = entityId,
                        discussionTopic = discussionTopic
                    )
                )
            }
    }

    private fun validateEntityId(entityId: UUID, discussionTopic: DiscussionTopic) : Boolean {

        return when (discussionTopic) {
            DiscussionTopic.EXERCISE -> lessonService.existsExerciseById(entityId);
            DiscussionTopic.PROJECT -> projectService.existsById(entityId);
        }

    }

    fun getDiscussionByEntity(
        entityId: UUID,
        discussionTopic: DiscussionTopic
    ): DiscussionResponse {
        val discussion = discussionRepository
            .findByEntityIdAndDiscussionTopic(entityId, discussionTopic)

        val messages = discussion?.let {
            discussionMessageRepository.findByDiscussionIdOrderByCreatedAtAsc(it.id)
        } ?: emptyList()

        val authorIds = messages.map { it.authorId }.toSet()

        val users = userService.findAllById(authorIds.toList())
            .associateBy { it.id }

        return DiscussionResponse(
            id = discussion?.id,
            entityId = entityId,
            discussionTopic = discussionTopic,
            children = discussionMessageMapper.toDiscussionMessageResponseList(messages, users)
        )
    }

    @Transactional
    fun createMessage (userId: UUID, req: CreateDiscussionMessageRequest) : DiscussionMessageResponse {

        val discussion = getOrCreateDiscussion(req.entityId, req.discussionTopic)

        val message = discussionMessageRepository.save(DiscussionMessage(id = UUID.randomUUID(), discussionId = discussion.id,
            authorId = userId, parentId = req.parentId, content = req.content, createdAt = OffsetDateTime.now(clock)))

        val authorId = message.authorId
        val user = userService.getSummaryById(authorId)

        return discussionMessageMapper.toDiscussionMessageResponse(message, user)

    }

}