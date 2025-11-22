package com.ludocode.ludocodebackend.ai.api.dto.response

data class AIMessagePart(
    val type: String = "text",
    val text: String
)