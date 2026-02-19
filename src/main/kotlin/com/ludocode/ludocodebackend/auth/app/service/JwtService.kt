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
import java.util.*

@Component
class JwtService(
) {
    private val expirationMillis = 1000L * 60 * 60 * 24

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    private fun getSigningKey(): Key =
        Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))

    internal fun createToken(userId: UUID, role: String?): String =
        Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", role)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationMillis))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()

    internal fun parseClaims(token: String): Claims {
        if (token.isBlank())
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token")

        return try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: JwtException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
        }
    }

}