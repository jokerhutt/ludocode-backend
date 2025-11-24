package com.ludocode.ludocodebackend.ai.api.dto.vercel.request

import com.ludocode.ludocodebackend.ai.domain.enums.AiMessageRole
import com.ludocode.ludocodebackend.ai.domain.enums.ChatType
import java.util.UUID

data class UIMessageRequestMetadata(
    val chatType: ChatType,
    val targetId: UUID?
)

sealed class UIMessagePart {
    data class Text(
        val type: String = "text",
        val text: String
    ) : UIMessagePart()
}

data class UIMessageRequest(
    val id: UUID,
    val role: AiMessageRole,
    val parts: List<UIMessagePart>,
    val metadata: UIMessageRequestMetadata?
)