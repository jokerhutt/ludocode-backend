package com.ludocode.ludocodebackend.auth.api.controller

import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.auth.app.service.AuthService
import com.ludocode.ludocodebackend.auth.configuration.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.AUTH.BASE)
class DemoAuthController(
    private val demoConfig: DemoConfig,
    private val authService: AuthService
) {

    @Operation(summary = "Authenticate user using demo account token",
        description = """
        Authenticates a user using a demo access token.
        If the demo user does not exist, a new demo account is created.
        On success, a session cookie is set and the authenticated user is returned. 
        """
        )
    @GetMapping(ApiPaths.AUTH.DEMO)
    fun loginDemo(
        @RequestParam token: String,
        response: HttpServletResponse
    ): ResponseEntity<UserLoginResponse> {

        if (token != demoConfig.token) {
            return ResponseEntity.status(403).body(null)
        }

        val loginResponse = authService.loginWithDemo(response)
        return ResponseEntity.ok(loginResponse)
    }

}