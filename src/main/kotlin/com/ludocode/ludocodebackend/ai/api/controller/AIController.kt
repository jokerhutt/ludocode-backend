package com.ludocode.ludocodebackend.ai.api.controller
import com.fasterxml.jackson.databind.ObjectMapper
import com.ludocode.ludocodebackend.ai.api.dto.response.ChatMessageResponse
import com.ludocode.ludocodebackend.ai.api.dto.vercel.request.ChatRequestBody
import com.ludocode.ludocodebackend.ai.api.dto.vercel.request.UIMessagePart
import com.ludocode.ludocodebackend.ai.app.service.AIService
import com.ludocode.ludocodebackend.ai.domain.enums.ChatType
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.UUID
import org.slf4j.LoggerFactory

@RestController
@RequestMapping(PathConstants.AI)
class AIController(private val aIService: AIService, private val objectMapper: ObjectMapper) {

    @PostMapping(
        value = [PathConstants.AI_SEND_PROMPT],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun handleChat(
        @RequestBody body: ChatRequestBody,
        @AuthenticationPrincipal(expression = "userId") userId: UUID?
    ): Flux<String> {

        if (userId == null) {
            return Flux.just("Error: Unauthenticated\n")
        }

        val last = body.messages.lastOrNull()
            ?: return Flux.just("Error: No messages\n")

        val text = last.parts
            .filter { it.type == "text" }
            .mapNotNull { it.text }
            .joinToString("")
            .takeIf { it.isNotBlank() }
            ?: return Flux.just("Error: Empty message\n")

        val chatType = last.metadata?.chatType
        val targetId = last.metadata?.targetId

        return aIService.streamTokens(
            req = text,
            chatType = chatType,
            targetId = targetId,
            userId = userId
        )
            .map { it.text } // raw tokens
            .onErrorResume { e ->
                Flux.just("Error: ${e.message}\n")
            }
    }

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