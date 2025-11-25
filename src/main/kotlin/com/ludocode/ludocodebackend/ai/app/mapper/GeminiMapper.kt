package com.ludocode.ludocodebackend.ai.app.mapper

import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiContent
import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiPart
import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiRequest
import com.ludocode.ludocodebackend.ai.api.dto.response.GeminiResponse
import org.springframework.stereotype.Component

@Component
class GeminiMapper {

    fun mapToGemini(prompt: String): GeminiRequest {
        return GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )
    }

}