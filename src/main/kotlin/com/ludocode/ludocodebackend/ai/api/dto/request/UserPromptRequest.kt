package com.ludocode.ludocodebackend.ai.api.dto.request

data class UserPromptRequest(val fileName: String, val fileContent: String, val userPrompt: String)
