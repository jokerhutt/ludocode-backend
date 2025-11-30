package com.ludocode.ludocodebackend.ai.configuration

import com.ludocode.ludocodebackend.ai.infra.http.AIModelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@Profile("!test")
class GeminiConfig(
    @Value("\${ai.api.key}") private val apiKey: String,
    @Value("\${ai.model}") private val model: String,
    private val builder: WebClient.Builder
) {
    @Bean
    fun geminiClient(): AIModelClient =
        AIModelClient(apiKey, model, builder)
}