package com.ludocode.ludocodebackend.auth.api.controller

import com.ludocode.ludocodebackend.auth.api.dto.TokenDto
import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.auth.app.service.AuthService
import com.ludocode.ludocodebackend.auth.configuration.AuthCookieConfig
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.commons.constants.PathConstants.FIREBASE_LOGIN
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.AUTH)
class AuthController(private val authService: AuthService, private val cookieConfig: AuthCookieConfig) {

    @PostMapping(FIREBASE_LOGIN)
    fun loginWithFirebase(
        @RequestHeader("Authorization") authHeader: String,
        response: HttpServletResponse
    ): ResponseEntity<UserLoginResponse> {

        val token = authHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            throw ApiException(ErrorCode.BAD_REQ, "Missing Firebase token")
        }

        return ResponseEntity.ok(authService.loginWithFirebase(response, token))

    }

    @GetMapping(PathConstants.AUTH_ME)
    fun getCurrentUser(
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ) : ResponseEntity<UserResponse> {
        return ResponseEntity.ok(authService.getAuthenticatedUser(userId))
    }

    @PostMapping(PathConstants.LOGOUT)
    fun logout(response: HttpServletResponse): ResponseEntity<Unit> {
        val c = cookieConfig
        val cookie = ResponseCookie.from(c.name, "")
            .httpOnly(true)
            .secure(c.secure)
            .sameSite(c.sameSite)
            .path(c.path)
            .domain(c.domain)
            .maxAge(0)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())

        return ResponseEntity.noContent().build()
    }


}