package com.ludocode.ludocodebackend.auth.api.controller.external

import com.ludocode.ludocodebackend.auth.api.dto.TokenDto
import com.ludocode.ludocodebackend.auth.app.service.AuthService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.AUTH)
class AuthController(private val authService: AuthService) {

    @PostMapping(PathConstants.GOOGLE_LOGIN)
    fun loginWithGoogle(
        @RequestBody tokenDto: TokenDto, response: HttpServletResponse
    ) : ResponseEntity<UserResponse> {
        return ResponseEntity.ok(authService.loginWithGoogle(tokenDto.code, response))
    }

    @GetMapping(PathConstants.AUTH_ME)
    fun getCurrentUser(
        @AuthenticationPrincipal(expression = "id") userId: UUID
    ) : ResponseEntity<UserResponse> {
        return ResponseEntity.ok(authService.getAuthenticatedUser(userId))
    }


}