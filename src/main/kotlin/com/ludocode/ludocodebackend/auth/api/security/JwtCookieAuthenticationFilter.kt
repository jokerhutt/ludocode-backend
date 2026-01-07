package com.ludocode.ludocodebackend.auth.api.security

import com.ludocode.ludocodebackend.auth.api.dto.AuthUser
import com.ludocode.ludocodebackend.auth.app.service.AuthCookieService
import com.ludocode.ludocodebackend.auth.app.service.JwtService
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtCookieAuthenticationFilter(
    private val authCookieService: AuthCookieService,
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {

        val path = request.requestURI
        return path.startsWith("/internal") ||
                path.startsWith("/api/v1/google-login") ||
                path.startsWith("/api/v1/auth/firebase") ||
                path.startsWith("/api/v1/firebase") ||
                path.startsWith("/actuator")
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        val token = authCookieService.readJwt(req)

        if (token != null) {
            try {
                val userId = jwtService.requireUserId(token)

                val userExists = userRepository.existsByIdAndIsDeletedFalse(userId)
                if (!userExists) {
                    SecurityContextHolder.clearContext()
                    chain.doFilter(req, res)
                    return
                }

                val principal = AuthUser(userId)
                val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList<GrantedAuthority>())
                auth.details = WebAuthenticationDetailsSource().buildDetails(req)
                SecurityContextHolder.getContext().authentication = auth
            } catch (e: Exception) {
            println("JWT invalid: ${e.message}")
        }
        }
        chain.doFilter(req, res)
    }
}