package com.ludocode.ludocodebackend.ai.infra.http

import com.fasterxml.jackson.databind.JsonNode
import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiRequest
import com.ludocode.ludocodebackend.ai.app.port.out.AIPort
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@ConditionalOnProperty(prefix = "ai", name = ["enabled"], havingValue = "true")
class AIModelClient(
    apiKey: String,
    private val model: String,
    builder: WebClient.Builder
) : AIPort {

    private val logger = LoggerFactory.getLogger(AIModelClient::class.java)

    private val client = builder
        .baseUrl("https://generativelanguage.googleapis.com")
        .defaultHeader("x-goog-api-key", apiKey)
        .build()

    override fun stream(request: GeminiRequest): Flux<String> {
        val streamUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent"

        logger.debug(
            LogEvents.AI_STREAM_STARTED + " {}",
            kv(LogFields.LANGUAGE, model)
        )

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