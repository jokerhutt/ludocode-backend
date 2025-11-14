package com.ludocode.ludocodebackend.auth.infra.http

import com.ludocode.ludocodebackend.auth.api.dto.GoogleTokenResponse
import com.ludocode.ludocodebackend.auth.app.port.out.GoogleAuthOutboundPort
import com.ludocode.ludocodebackend.commons.constants.ExternalPathContstants
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class GoogleOAuthClient(
    @Value("\${google.client-id}")     private val clientId: String,
    @Value("\${google.client-secret}") private val clientSecret: String,
    @Value("\${google.redirect-uri}")  private val redirectUri: String
) : GoogleAuthOutboundPort {

    private val restTemplate = RestTemplate()

    override fun exchangeCodeForAccessToken(code: String): GoogleTokenResponse {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("code", code)
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", redirectUri)
            add("grant_type", "authorization_code")
        }
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        val req = HttpEntity(form, headers)

        return restTemplate.postForObject(
            ExternalPathContstants.GOOGLE_OAUTH_TOKEN,
            req,
            GoogleTokenResponse::class.java
        ) ?: throw IllegalStateException("Google token exchange returned null body")
    }
}