package com.ludocode.ludocodebackend.ai.app.service
import com.ludocode.ludocodebackend.ai.api.dto.internal.ChatPartsTuple
import com.ludocode.ludocodebackend.ai.api.dto.response.AIMessagePart
import com.ludocode.ludocodebackend.ai.api.dto.vercel.request.UIMessagePart
import com.ludocode.ludocodebackend.ai.api.dto.vercel.request.UIMessageRequest
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

    fun streamTokens(messageHistory: List<UIMessageRequest>, chatType: ChatType?, targetId: UUID?, userId: UUID): Flux<AIMessagePart> {

        val credits = aICreditService.initializeOrGetCredits(userId)
        if (credits.credits <= 0) throw ApiException(ErrorCode.NOT_ENOUGH_CREDITS)
        aICreditService.handleDeductCredits(userId)

        val chatTuple = getHistoryAndLast(messageHistory)
        val userMessage = chatTuple.last
        val chatHistory = chatTuple.history

        val prompt = getPrompt(userMessage, chatHistory, targetId, chatType ?: ChatType.DEFAULT)

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

    private fun getHistoryAndLast(messages: List<UIMessageRequest>): ChatPartsTuple {
        val roleTaggedTexts = messages.mapNotNull { msg ->
            val combinedText = msg.parts
                .filter { it.type == "text" }
                .mapNotNull { it.text }
                .joinToString(" ")
                .takeIf { it.isNotBlank() }

            if (combinedText == null) null
            else "${msg.role}: $combinedText"
        }

        if (roleTaggedTexts.isEmpty()) {
            return ChatPartsTuple(emptyList(), "")
        }

        val history = roleTaggedTexts.dropLast(1)
        val last = roleTaggedTexts.last()

        return ChatPartsTuple(history, last)
    }

    private fun getPrompt (userPrompt: String, chatHistory: List<String>, targetId: UUID?, chatType: ChatType) : String {
        when (chatType) {
            ChatType.DEFAULT -> return aIPromptBuilder.buildGenericPrompt(userPrompt, chatHistory)
            ChatType.LESSON -> {
                if (targetId == null) return aIPromptBuilder.buildGenericPrompt(userPrompt, chatHistory)
                val exerciseContent = getExerciseContent(exerciseId = targetId)
                return aIPromptBuilder.buildLessonPrompt(userPrompt, exerciseContent, chatHistory)
            }
            ChatType.PROJECT -> {
                val fileContent = targetId?.let { getFileContent(it) } ?: ""
                return aIPromptBuilder.buildProjectPrompt(userPrompt, fileContent, chatHistory)
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