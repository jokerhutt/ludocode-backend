package com.ludocode.ludocodebackend.ai.infra.http

import com.ludocode.ludocodebackend.ai.api.dto.request.GeminiRequest
import com.ludocode.ludocodebackend.ai.api.dto.response.GeminiResponse
import com.ludocode.ludocodebackend.ai.app.port.out.AIPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AIModelClient(
    @Value("\${ai.api.key}") private val apiKey: String,
    @Value("\${ai.model}") private val model: String
) : AIPort {

    private val rest = RestTemplate()

    override fun execute(request: GeminiRequest): GeminiResponse {
        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val entity = HttpEntity(request, headers)

        val response = rest.postForObject(
            url,
            entity,
            GeminiResponse::class.java
        ) ?: throw IllegalStateException("AI model returned null body")

        return response
    }
}