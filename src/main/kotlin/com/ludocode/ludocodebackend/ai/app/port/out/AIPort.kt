package com.ludocode.ludocodebackend.ai.app.port.out
import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiRequest
import com.ludocode.ludocodebackend.ai.api.dto.response.GeminiResponse

interface AIPort {
    fun execute(request: GeminiRequest): GeminiResponse
}