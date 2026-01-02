package com.ludocode.ludocodebackend.auth.api.controller

import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.auth.app.service.AuthService
import com.ludocode.ludocodebackend.auth.configuration.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PathConstants.AUTH)
class DemoAuthController(
    private val demoConfig: DemoConfig,
    private val authService: AuthService
) {

    @GetMapping(PathConstants.DEMO_LOGIN)
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