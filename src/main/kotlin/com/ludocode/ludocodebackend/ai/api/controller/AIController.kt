package com.ludocode.ludocodebackend.ai.api.controller

import com.ludocode.ludocodebackend.ai.api.dto.request.ChatRequestBody
import com.ludocode.ludocodebackend.ai.app.service.AIService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.UUID

@RestController
@RequestMapping(PathConstants.AI)
class AIController(private val aIService: AIService) {

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

        val chatType = last.metadata?.chatType
        val targetId = last.metadata?.targetId

        return aIService.streamTokens(
            messageHistory = body.messages,
            chatType = chatType,
            targetId = targetId,
            userId = userId
        )
            .map { it.text }
            .onErrorResume { e ->
                Flux.just("Error: ${e.message}\n")
            }
    }


}