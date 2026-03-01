package com.ludocode.ludocodebackend.ai.app.service

import com.ludocode.ludocodebackend.ai.api.dto.internal.ChatPartsTuple
import com.ludocode.ludocodebackend.ai.api.dto.request.UIMessageRequest
import com.ludocode.ludocodebackend.ai.api.dto.response.AIMessagePart
import com.ludocode.ludocodebackend.ai.app.mapper.GeminiMapper
import com.ludocode.ludocodebackend.ai.app.port.out.AIPort
import com.ludocode.ludocodebackend.ai.domain.enums.ChatType
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForAI
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.projects.app.port.`in`.ProjectsPortForAI
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

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

    private val logger = LoggerFactory.getLogger(AIService::class.java)


    fun streamTokens(
        messageHistory: List<UIMessageRequest>,
        chatType: ChatType?,
        targetId: UUID?,
        userId: UUID
    ): Flux<AIMessagePart> {

        val resolvedType = chatType ?: ChatType.DEFAULT

        return withMdc(
            LogFields.USER_ID to userId.toString(),
            LogFields.CHAT_TYPE to resolvedType.toString(),
            LogFields.AI_TARGET_ID to targetId.toString()
        ) {
            val credits = aICreditService.initializeOrGetCredits(userId)
            if (credits.credits <= 0) {
                logger.warn(LogEvents.AI_CREDITS_EXHAUSTED)
                throw ApiException(ErrorCode.NOT_ENOUGH_CREDITS)
            }
            aICreditService.deductCredits(userId)

            val chatTuple = getHistoryAndLast(messageHistory)
            val userMessage = chatTuple.last
            val chatHistory = chatTuple.history

            val prompt = getPrompt(userMessage, chatHistory, targetId, resolvedType)

            val geminiRequest = geminiMapper.mapToGemini(prompt)

            logger.info(
                LogEvents.AI_STREAM_STARTED + " {} {}",
                kv(LogFields.HISTORY_COUNT, chatHistory.size),
                kv(LogFields.USER_MESSAGE_LENGTH, userMessage.length),
            )

            aIPort.stream(geminiRequest)
                .map { token ->
                    AIMessagePart(type = "text", text = token)
                }
                .doOnComplete {
                    logger.info(LogEvents.AI_STREAM_COMPLETED)
                }
                .doOnError { e ->
                    logger.error(LogEvents.AI_STREAM_FAILED, e)
                }
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

    private fun getPrompt(userPrompt: String, chatHistory: List<String>, targetId: UUID?, chatType: ChatType): String {
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

    private fun getFileContent(fileId: UUID): String {
        return projectsPortForAI?.getFileContentById(fileId) ?: ""
    }

    private fun getExerciseContent(exerciseId: UUID): ExerciseSnap {
        return catalogPortForAI.findExerciseSnapshotById(exerciseId)
    }


}