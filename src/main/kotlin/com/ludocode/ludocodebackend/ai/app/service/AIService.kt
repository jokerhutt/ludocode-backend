package com.ludocode.ludocodebackend.ai.app.service

import com.ludocode.ludocodebackend.ai.api.dto.request.UserPromptRequest
import com.ludocode.ludocodebackend.ai.api.dto.response.AIResponsePacket
import com.ludocode.ludocodebackend.ai.app.mapper.GeminiMapper
import com.ludocode.ludocodebackend.ai.domain.entity.UserAICredits
import com.ludocode.ludocodebackend.ai.infra.http.AIModelClient
import com.ludocode.ludocodebackend.ai.infra.repository.UserAICreditsRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AIService(
    private val aIModelClient: AIModelClient,
    private val geminiMapper: GeminiMapper,
    private val aICreditService: AICreditService
) {

    fun executeUserPrompt (req: UserPromptRequest, userId: UUID): AIResponsePacket {

        var userCredits = aICreditService.initializeOrGetCredits(userId)
        if (userCredits.credits <= 0) throw ApiException(ErrorCode.NOT_ENOUGH_CREDITS)
        val newCredits = aICreditService.handleDeductCredits(userId)

        val prompt = buildPrompt(req)
        val request = geminiMapper.mapToGemini(prompt)

        val aiClientResponse = aIModelClient.execute(request)
        val responseForFrontend = geminiMapper.mapToResponse(aiClientResponse)

        return AIResponsePacket(responseForFrontend, newCredits)

    }

    fun buildPrompt(req: UserPromptRequest): String =
        """
    You are the Ludocode tutor.
    The user asks: ${req.userPrompt}

    Code:
    ${req.fileContent}

    Respond with:
    - Explanation
    - Hints
    - Fixed code
    """.trimIndent()

}