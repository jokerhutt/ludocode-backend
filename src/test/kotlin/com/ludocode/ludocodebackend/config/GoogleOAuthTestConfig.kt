package com.ludocode.ludocodebackend.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ludocode.ludocodebackend.auth.api.dto.GoogleTokenResponse
import com.ludocode.ludocodebackend.auth.app.port.out.GoogleAuthOutboundPort
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.util.Base64

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
                    idToken = fakeJwt(
                        mapOf(
                            "sub" to MockOauthConstants.USER_1_GOOGLE_SUB,
                            "email" to "email@google.com",
                            "given_name" to "John",
                            "family_name" to "Doe",
                            "picture" to "https://example.com/avatar.png"
                        )
                    )
                )
            }
        }

    private fun fakeJwt(claims: Map<String, Any>): String {
        val header = """{"alg":"RS256","typ":"JWT"}"""
        val payload = jacksonObjectMapper().writeValueAsString(claims)
        val signature = "fake-signature"

        fun b64(s: String) =
            Base64.getUrlEncoder().withoutPadding().encodeToString(s.toByteArray())

        return "${b64(header)}.${b64(payload)}.${b64(signature)}"
    }
}