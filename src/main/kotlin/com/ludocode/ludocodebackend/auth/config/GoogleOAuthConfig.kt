package com.ludocode.ludocodebackend.auth.config

import com.ludocode.ludocodebackend.auth.app.port.out.GoogleAuthOutboundPort
import com.ludocode.ludocodebackend.auth.infra.http.GoogleOAuthClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class GoogleOAuthConfig(
    @Value("\${google.client-id}") private val clientId: String,
    @Value("\${google.client-secret}") private val clientSecret: String,
    @Value("\${google.redirect-uri}") private val redirectUri: String
) {

    @Bean
    fun googleOAuthClient(): GoogleAuthOutboundPort =
        GoogleOAuthClient(
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUri = redirectUri
        )
}