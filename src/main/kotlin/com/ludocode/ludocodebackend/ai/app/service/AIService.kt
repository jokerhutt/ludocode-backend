package com.ludocode.ludocodebackend.ai.app.service
import com.ludocode.ludocodebackend.ai.api.dto.response.AIMessagePart
import com.ludocode.ludocodebackend.ai.app.mapper.GeminiMapper
import com.ludocode.ludocodebackend.ai.infra.http.AIModelClient
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.UUID

@Service
class AIService(
    private val aIModelClient: AIModelClient,
    private val geminiMapper: GeminiMapper,
    private val aICreditService: AICreditService
) {


    fun streamTokens(req: String, file: String, userId: UUID): Flux<AIMessagePart> {



        val credits = aICreditService.initializeOrGetCredits(userId)
        if (credits.credits <= 0) throw ApiException(ErrorCode.NOT_ENOUGH_CREDITS)
        aICreditService.handleDeductCredits(userId)

        println("Deducted")

        val prompt = buildPrompt(req)
        println("PROMPT: $prompt")

        val geminiRequest = geminiMapper.mapToGemini(prompt)

        return aIModelClient.stream(geminiRequest)
            .map { token ->
                println("Token: $token")
                AIMessagePart(
                    type = "text",
                    text = token
                )
            }
    }

    private fun buildPrompt(req: String): String =
        """
        You are the Ludocode tutor.
        The user asks: ${req}

        Respond with:
        - Explanation
        - Hints
        - Fixed code
        """.trimIndent()


}