package com.ludocode.ludocodebackend.ai.app.service
import com.ludocode.ludocodebackend.ai.api.dto.response.AIMessagePart
import com.ludocode.ludocodebackend.ai.app.mapper.GeminiMapper
import com.ludocode.ludocodebackend.ai.domain.enums.ChatType
import com.ludocode.ludocodebackend.ai.infra.client.CatalogClientForAI
import com.ludocode.ludocodebackend.ai.infra.client.ProjectsClientForAI
import com.ludocode.ludocodebackend.ai.infra.http.AIModelClient
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
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
    private val projectsClientForAI: ProjectsClientForAI,
    private val catalogClientForAI: CatalogClientForAI,
    private val aIPromptBuilder: AIPromptBuilder,
) {

    fun streamTokens(req: String, chatType: ChatType, targetId: UUID?, userId: UUID): Flux<AIMessagePart> {

        val credits = aICreditService.initializeOrGetCredits(userId)
        if (credits.credits <= 0) throw ApiException(ErrorCode.NOT_ENOUGH_CREDITS)
        aICreditService.handleDeductCredits(userId)

        val prompt = getPrompt(req, targetId, chatType)

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

    private fun getPrompt (userPrompt: String, targetId: UUID?, chatType: ChatType) : String {
        when (chatType) {
            ChatType.LESSON -> {
                if (targetId == null) return aIPromptBuilder.buildGenericPrompt(userPrompt)
                val exerciseContent = getExerciseContent(exerciseId = targetId)
                return aIPromptBuilder.buildLessonPrompt(userPrompt, exerciseContent)
            }
            ChatType.PROJECT -> {
                val fileContent = targetId?.let { getFileContent(it) } ?: ""
                return aIPromptBuilder.buildProjectPrompt(userPrompt, fileContent)
            }
        }
    }

    private fun getFileContent (fileId: UUID) : String {
        return projectsClientForAI.getFileContentById(fileId)
    }

    private fun getExerciseContent (exerciseId: UUID) : ExerciseSnap {
        return catalogClientForAI.findExerciseSnapshotById(exerciseId)
    }



}