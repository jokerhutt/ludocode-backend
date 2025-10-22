package com.ludocode.ludocodebackend.auth.app.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class AuthCookieService {

    @Value("\${auth.cookie.name:jwt}")
    private lateinit var cookieName: String   // not nullable

    @Value("\${auth.cookie.path:/}")
    private lateinit var cookiePath: String

    @Value("\${auth.cookie.domain:}")
    private lateinit var cookieDomain: String

    @Value("\${auth.cookie.same-site:Lax}")
    private lateinit var sameSite: String

    @Value("\${auth.cookie.secure:false}")
    private var secure: Boolean = false

    @Value("\${auth.cookie.max-age-seconds:86400}")
    private var defaultMaxAge: Long = 0

    fun setJwt(response: HttpServletResponse, jwt: String, maxAgeSeconds: Long = defaultMaxAge) {
        addCookie(response, jwt, maxAgeSeconds)
    }

    fun clearJwt(response: HttpServletResponse) {
        addCookie(response, "", 0)
    }

    fun readJwt(request: HttpServletRequest): String? =
        request.cookies?.firstOrNull { it.name == cookieName }?.value

    private fun addCookie(response: HttpServletResponse, value: String, maxAgeSeconds: Long) {
        val b = ResponseCookie.from(cookieName, value)
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)   // "Lax" for same-site localhost; use "None" + secure=true for cross-site
            .path(cookiePath)
            .maxAge(maxAgeSeconds)

        if (cookieDomain.isNotBlank()) {
            b.domain(cookieDomain)   // only set when nonblank; never set for localhost
        }

        val header = b.build().toString()
        response.addHeader(HttpHeaders.SET_COOKIE, header)
    }
}