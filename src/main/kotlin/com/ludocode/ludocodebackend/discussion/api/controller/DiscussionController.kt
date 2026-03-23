package com.ludocode.ludocodebackend.discussion.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.discussion.api.dto.CreateDiscussionMessageRequest
import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionMessageResponse
import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionResponse
import com.ludocode.ludocodebackend.discussion.api.dto.response.MessageLikeCountResponse
import com.ludocode.ludocodebackend.discussion.app.service.DiscussionMessageLikeService
import com.ludocode.ludocodebackend.discussion.app.service.DiscussionService
import com.ludocode.ludocodebackend.discussion.domain.enums.DiscussionTopic
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.DISCUSSION.BASE)
class DiscussionController(
    private val discussionService: DiscussionService,
    private val discussionMessageLikeService: DiscussionMessageLikeService,
) {


    @PostMapping
    fun createMessage (@Valid @RequestBody req: CreateDiscussionMessageRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<DiscussionMessageResponse> {
        return ResponseEntity.ok(discussionService.createMessage(userId, req))
    }

    @GetMapping("${ApiPaths.DISCUSSION.BY_ENTITY_ID}${ApiPaths.DISCUSSION.BY_TOPIC}")
    fun getDiscussion (@PathVariable entityId: UUID, @PathVariable topic: DiscussionTopic) : ResponseEntity<DiscussionResponse> {
        return ResponseEntity.ok(discussionService.getDiscussionByEntity(entityId, topic))
    }

    @PostMapping(ApiPaths.DISCUSSION.BY_ID_LIKE)
    fun likeMessage(
        @PathVariable messageId: UUID,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<MessageLikeCountResponse> {
        discussionMessageLikeService.likeMessage(userId, messageId)
        return ResponseEntity.ok(discussionMessageLikeService.getLikeCountByMessageId(userId, messageId))
    }

    @DeleteMapping(ApiPaths.DISCUSSION.BY_ID_LIKE)
    fun unlikeMessage(
        @PathVariable messageId: UUID,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<MessageLikeCountResponse> {
        discussionMessageLikeService.unlikeMessage(userId, messageId)
        return ResponseEntity.ok(discussionMessageLikeService.getLikeCountByMessageId(userId, messageId))
    }

    @GetMapping(ApiPaths.DISCUSSION.LIKE)
    fun getMessageLikeCountsByIds(
        @RequestParam messageIds: List<UUID>,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<List<MessageLikeCountResponse>> {
        return ResponseEntity.ok(discussionMessageLikeService.getLikeCountsByMessageIds(userId, messageIds))
    }

}