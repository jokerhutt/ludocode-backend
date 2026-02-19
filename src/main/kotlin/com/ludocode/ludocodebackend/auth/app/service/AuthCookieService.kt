package com.ludocode.ludocodebackend.auth.app.service

import com.ludocode.ludocodebackend.auth.configuration.AuthCookieConfig
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class AuthCookieService(private val cookieConfig: AuthCookieConfig) {

    internal fun setJwt(response: HttpServletResponse, jwt: String, maxAgeSeconds: Long = cookieConfig.maxAgeSeconds) {
        addCookie(response, jwt, maxAgeSeconds)
    }

    internal fun clearJwt(response: HttpServletResponse) {
        addCookie(response, "", 0)
    }

    internal fun readJwt(request: HttpServletRequest): String? =
        request.cookies?.firstOrNull { it.name == cookieConfig.name }?.value

    private fun addCookie(response: HttpServletResponse, value: String, maxAgeSeconds: Long) {
        val c = cookieConfig
        val b = ResponseCookie.from(c.name, value)
            .httpOnly(true)
            .secure(c.secure)
            .sameSite(c.sameSite)
            .path(c.path)
            .maxAge(maxAgeSeconds)

        if (c.domain.isNotBlank()) {
            b.domain(c.domain)
        }

        val header = b.build().toString()
        response.addHeader(HttpHeaders.SET_COOKIE, header)
    }
}