package com.ludocode.ludocodebackend.auth.api.controller

import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.auth.app.service.AuthService
import com.ludocode.ludocodebackend.auth.configuration.cookie.AuthCookieProperties
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(
    name = "Auth",
    description = "Operations related to authentication & account creation"
)
@RestController
@RequestMapping(ApiPaths.AUTH.BASE)
class AuthController(
    private val authService: AuthService,
    private val cookieConfig: AuthCookieProperties,
    private val subscriptionService: SubscriptionService
) {

    @Operation(
        summary = "Authenticate user using Firebase",
        description = """
        Authenticate a user using a Firebase ID token.
        If the user does not exist, a new account is created.
        On success, a session cookie is set and the authenticated user is returned. 
        """
    )
    @PostMapping(ApiPaths.AUTH.FIREBASE)
    fun loginWithFirebase(
        @RequestHeader("Authorization") authHeader: String,
        response: HttpServletResponse
    ): ResponseEntity<UserLoginResponse> {

        val token = authHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            throw ApiException(ErrorCode.BAD_REQ, "Missing Firebase token")
        }

        val userLoginResponse = authService.loginWithFirebase(response, token)
        subscriptionService.createCustomerId(userId = userLoginResponse.user.id)

        return ResponseEntity.ok(userLoginResponse)

    }

    @Operation(
        summary = "Get current authenticated user",
        description = """
        Returns the currently authenticated user associated with the active session.
        Requires a valid session cookie to be present. 
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping(ApiPaths.AUTH.ME)
    fun getCurrentUser(
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(authService.getAuthenticatedUser(userId))
    }

    @Operation(
        summary = "Log out the current user",
        description = """
        Logs out the currently authenticated user by expiring the session cookie.
        After this request, the user is no longer authenticated.   
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping(ApiPaths.AUTH.LOGOUT)
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