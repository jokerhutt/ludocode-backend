package com.ludocode.ludocodebackend.ai.app.service
import com.ludocode.ludocodebackend.ai.api.dto.response.AIMessagePart
import com.ludocode.ludocodebackend.ai.app.mapper.GeminiMapper
import com.ludocode.ludocodebackend.ai.infra.client.ProjectsClientForAI
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
    private val aICreditService: AICreditService,
    private val projectsClientForAI: ProjectsClientForAI
) {


    fun streamTokens(req: String, fileId: UUID?, userId: UUID): Flux<AIMessagePart> {

        println("A")

        val credits = aICreditService.initializeOrGetCredits(userId)
        if (credits.credits <= 0) throw ApiException(ErrorCode.NOT_ENOUGH_CREDITS)
        aICreditService.handleDeductCredits(userId)

        println("Deducted")
        val fileContent = fileId?.let { getFileContent(it) } ?: ""
        val prompt = buildPrompt(req, fileContent)
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


    private fun getFileContent (fileId: UUID) : String {
        return projectsClientForAI.getFileContentById(fileId)
    }

    private fun buildPrompt(req: String, fileContent: String): String =
        """
        You are a helpful and concise coding helper on a code learning app.
        The user asks: ${req}
        
        Their current file context is: ${fileContent}.
        
        Ensure that any code markdown is formatted according to the Vercel AI SDK dev requirements.

        Respond with:
        - A fitting answer to their request
        - Hints
        - Fixed code if required
        """.trimIndent()


}