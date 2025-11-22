package com.ludocode.ludocodebackend.ai.api.controller
import com.ludocode.ludocodebackend.ai.api.dto.response.AIMessagePart
import com.ludocode.ludocodebackend.ai.app.service.AIService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
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
class AIController(private val aIService: AIService) {

    @GetMapping(PathConstants.AI_SEND_PROMPT, produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamPrompt(
        @RequestParam prompt: String,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): Flux<ServerSentEvent<AIMessagePart>> {
        println("Called Controller")
        return aIService.streamTokens(prompt, userId)
            .map { part ->
                ServerSentEvent.builder(part)
                    .build()
            }
    }

}