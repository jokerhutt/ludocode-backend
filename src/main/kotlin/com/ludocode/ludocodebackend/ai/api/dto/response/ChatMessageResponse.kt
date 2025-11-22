package com.ludocode.ludocodebackend.ai.api.dto.response

data class ChatMessageResponse (    val id: String,
                               val role: String = "assistant",
                               val part: AIMessagePart)