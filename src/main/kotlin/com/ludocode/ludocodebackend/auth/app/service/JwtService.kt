package com.ludocode.ludocodebackend.auth.app.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.Date
import java.util.UUID

@Component
class JwtService(
) {
    private val expirationMillis = 1000L * 60 * 60 * 24

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    private fun getSigningKey(): Key =
        Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))

    internal fun createToken(userId: UUID): String =
        Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationMillis))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()

    internal fun requireUserId(token: String): UUID {
        if (token.isBlank())
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token")

        return try {
            val claims: Claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .body

            UUID.fromString(claims.subject)
        } catch (e: JwtException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
        }
    }
}