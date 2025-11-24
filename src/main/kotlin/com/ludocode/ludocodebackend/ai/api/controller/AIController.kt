package com.ludocode.ludocodebackend.ai.api.controller
import com.fasterxml.jackson.databind.ObjectMapper
import com.ludocode.ludocodebackend.ai.api.dto.response.ChatMessageResponse
import com.ludocode.ludocodebackend.ai.app.service.AIService
import com.ludocode.ludocodebackend.ai.domain.enums.ChatType
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.UUID


@RestController
@RequestMapping(PathConstants.AI)
class AIController(private val aIService: AIService, private val objectMapper: ObjectMapper) {

    @GetMapping(PathConstants.AI_SEND_PROJECT_PROMPT, produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamProjectPrompt(
        @RequestParam prompt: String,
        @RequestParam(required = false) fileId: UUID?,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): Flux<ServerSentEvent<String>> {

        if (userId == null) throw ApiException(ErrorCode.USER_NOT_FOUND)

        val messageId = UUID.randomUUID().toString()

        return aIService.streamTokens(prompt, ChatType.PROJECT, fileId, userId)
            .map { part ->
                val response = ChatMessageResponse(
                    id = messageId,
                    role = "assistant",
                    parts = listOf(part)
                )

                ServerSentEvent.builder(
                    objectMapper.writeValueAsString(response)
                ).build()
            }
    }

    @GetMapping(PathConstants.AI_SEND_EXERCISE_PROMPT, produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamExercisePrompt(
        @RequestParam prompt: String,
        @RequestParam exerciseId: UUID,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): Flux<ServerSentEvent<String>> {

        if (userId == null) throw ApiException(ErrorCode.USER_NOT_FOUND)

        val messageId = UUID.randomUUID().toString()

        return aIService.streamTokens(prompt, ChatType.LESSON, exerciseId, userId)
            .map { part ->
                val response = ChatMessageResponse(
                    id = messageId,
                    role = "assistant",
                    parts = listOf(part)
                )

                ServerSentEvent.builder(
                    objectMapper.writeValueAsString(response)
                ).build()
            }
    }



}