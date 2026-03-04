package com.ludocode.ludocodebackend.ai.api.controller

import com.ludocode.ludocodebackend.ai.api.dto.request.ChatRequestBody
import com.ludocode.ludocodebackend.ai.app.service.AIService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.*

@Tag(
    name = "AI",
    description = "Operations related to prompting the chatbot"
)
@ConditionalOnProperty(prefix = "ai", name = ["enabled"], havingValue = "true")
@RestController
@RequestMapping(ApiPaths.AI.BASE)
class AIController(private val aIService: AIService) {


    @Operation(
        summary = "Generate AI completion",
        description = """
        Processes a chat-style request and generates an AI completion.
        Accepts a sequence of messages representing the conversation context and returns the generated response output. 
        """
    )
    @PostMapping(
        value = [ApiPaths.AI.COMPLETIONS],
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

        val systemPrompt = last.metadata?.systemPrompt ?: throw ApiException(ErrorCode.SYSTEM_PROMPT_MISSING)
        val promptWrapper = last.metadata?.promptWrapper

        return aIService.streamTokens(
            messageHistory = body.messages,
            systemPrompt = systemPrompt,
            promptWrapper = promptWrapper,
            userId = userId
        )
            .map { it.text }
            .onErrorResume { e ->
                Flux.just("Error: ${e.message}\n")
            }
    }


}