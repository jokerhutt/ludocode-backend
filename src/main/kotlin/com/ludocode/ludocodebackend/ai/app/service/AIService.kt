package com.ludocode.ludocodebackend.ai.app.service
import com.ludocode.ludocodebackend.ai.api.dto.internal.ChatPartsTuple
import com.ludocode.ludocodebackend.ai.api.dto.response.AIMessagePart
import com.ludocode.ludocodebackend.ai.api.dto.request.UIMessageRequest
import com.ludocode.ludocodebackend.ai.app.mapper.GeminiMapper
import com.ludocode.ludocodebackend.ai.app.port.out.AIPort
import com.ludocode.ludocodebackend.ai.domain.enums.ChatType
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForAI
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.playground.app.port.`in`.ProjectsPortForAI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.UUID

@ConditionalOnProperty(prefix = "ai", name = ["enabled"], havingValue = "true")
@Service
class AIService(
    private val geminiMapper: GeminiMapper,
    private val aICreditService: AICreditService,
    private val aIPromptBuilder: AIPromptBuilder,
    private val aIPort: AIPort,
    private val projectsPortForAI: ProjectsPortForAI,
    private val catalogPortForAI: CatalogPortForAI,
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

        return aIPort.stream(geminiRequest)
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
        return projectsPortForAI?.getFileContentById(fileId) ?: ""
    }

    private fun getExerciseContent (exerciseId: UUID) : ExerciseSnap {
        return catalogPortForAI.findExerciseSnapshotById(exerciseId)
    }



}