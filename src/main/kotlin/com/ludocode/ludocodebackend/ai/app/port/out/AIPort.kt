package com.ludocode.ludocodebackend.ai.app.port.out
import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiRequest
import com.ludocode.ludocodebackend.ai.api.dto.response.GeminiResponse
import reactor.core.publisher.Flux

interface AIPort {
    fun stream(request: GeminiRequest): Flux<String>
}