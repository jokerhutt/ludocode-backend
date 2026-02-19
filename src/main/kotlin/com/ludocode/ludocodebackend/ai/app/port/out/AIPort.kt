package com.ludocode.ludocodebackend.ai.app.port.out

import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiRequest
import reactor.core.publisher.Flux

interface AIPort {
    fun stream(request: GeminiRequest): Flux<String>
}