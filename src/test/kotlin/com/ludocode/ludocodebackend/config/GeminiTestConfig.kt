package com.ludocode.ludocodebackend.config

import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiRequest
import com.ludocode.ludocodebackend.ai.app.port.out.AIPort
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Flux

@TestConfiguration
class GeminiTestConfig {

    @Bean
    fun geminiClient(): AIPort = object : AIPort {
        override fun stream(request: GeminiRequest): Flux<String> {
            return Flux.just("test-value")
        }
    }
}