package com.ludocode.ludocodebackend.ai.infra.http
import com.fasterxml.jackson.databind.JsonNode
import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiRequest
import com.ludocode.ludocodebackend.ai.app.port.out.AIPort
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

class AIModelClient(
    apiKey: String,
    private val model: String,
    builder: WebClient.Builder
) : AIPort {

    private val client = builder
        .baseUrl("https://generativelanguage.googleapis.com")
        .defaultHeader("x-goog-api-key", apiKey)
        .build()

    override fun stream(request: GeminiRequest): Flux<String> {
        println("In Client of Gemini")
        val streamUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent"

        return client.post()
            .uri(streamUrl)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(JsonNode::class.java)
            .flatMap { chunk -> extractTokens(chunk) }
    }

    fun extractTokens(node: JsonNode): Flux<String> {
        val candidates = node["candidates"] ?: return Flux.empty()

        val tokens = candidates.flatMap { cand ->
            val parts = cand["content"]?.get("parts") ?: return@flatMap emptyList()
            parts.mapNotNull { it["text"]?.asText() }
        }

        return Flux.fromIterable(tokens)
    }
}