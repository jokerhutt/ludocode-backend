package com.ludocode.ludocodebackend.ai.api.dto.request
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ludocode.ludocodebackend.ai.domain.enums.AiMessageRole
import com.ludocode.ludocodebackend.ai.domain.enums.ChatType
import java.util.UUID



data class UIMessagePart(
    val type: String,
    val text: String?
)


data class UIMessageRequestMetadata(
    val chatType: ChatType,
    val targetId: UUID?
)

data class UIMessageRequest(
    val id: String,
    val role: AiMessageRole,
    val parts: List<UIMessagePart>,
    val metadata: UIMessageRequestMetadata?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatRequestBody(
    val id: String,
    val messages: List<UIMessageRequest>
)