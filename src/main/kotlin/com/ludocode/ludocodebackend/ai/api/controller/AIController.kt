package com.ludocode.ludocodebackend.ai.api.controller

import com.ludocode.ludocodebackend.ai.api.dto.request.UserPromptRequest
import com.ludocode.ludocodebackend.ai.api.dto.response.AIResponsePacket
import com.ludocode.ludocodebackend.ai.app.service.AIService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping(PathConstants.AI)
class AIController(private val aIService: AIService) {

    @PostMapping(PathConstants.AI_SEND_PROMPT)
    fun sendPrompt (@RequestBody req: UserPromptRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID): ResponseEntity<AIResponsePacket> {
        return ResponseEntity.ok(aIService.executeUserPrompt(req, userId))
    }

}