package com.ludocode.ludocodebackend.config

import com.ludocode.ludocodebackend.auth.api.dto.GoogleTokenResponse
import com.ludocode.ludocodebackend.auth.app.port.out.GoogleAuthOutboundPort
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class GoogleOAuthTestConfig {

    @Bean
    fun googleOAuthClient(): GoogleAuthOutboundPort =
        object : GoogleAuthOutboundPort {
            override fun exchangeCodeForAccessToken(code: String): GoogleTokenResponse {
                return GoogleTokenResponse(
                    accessToken = "fake-access-token",
                    expiresIn = 3600,
                    refreshToken = "fake-refresh-token",
                    scope = "email profile",
                    tokenType = "Bearer",
                    idToken = "fake-id-token"
                )
            }
        }
}