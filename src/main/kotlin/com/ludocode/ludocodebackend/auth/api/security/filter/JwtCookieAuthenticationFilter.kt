package com.ludocode.ludocodebackend.auth.api.security.filter

import com.ludocode.ludocodebackend.auth.api.security.principal.AuthUser
import com.ludocode.ludocodebackend.auth.app.service.AuthCookieService
import com.ludocode.ludocodebackend.auth.app.service.AuthService
import com.ludocode.ludocodebackend.auth.app.service.JwtService
import com.ludocode.ludocodebackend.auth.configuration.firebase.FirebaseProperties
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.UUID

@Component
class JwtCookieAuthenticationFilter(
    private val authCookieService: AuthCookieService,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val firebaseProperties: FirebaseProperties,
    @Lazy private val authService: AuthService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtCookieAuthenticationFilter::class.java)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {

        val path = request.requestURI
        val exclude = path.startsWith("/internal") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api/v1/auth/firebase") ||
                path.startsWith("/api/v1/firebase") ||
                path.startsWith("/actuator") ||
                path.startsWith("/api/v1/subscription/webhook")
                path.startsWith("/api/v1/schemas") ||
                        path.startsWith("/api/v1/schemas")

        log.debug("shouldNotFilter check: path='$path', exclude=$exclude")
        return exclude
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        var token = authCookieService.readJwt(req)

        // When Firebase is disabled, auto-login as the demo user if there's no active session
        if (token == null && !firebaseProperties.enabled) {
            try {
                val loginResponse = authService.loginWithDemo(res)
                token = jwtService.createToken(loginResponse.user.id, role = "admin")
            } catch (e: Exception) {
                log.warn("Demo auto-login failed: ${e.message}")
            }
        }

        if (token != null) {
            try {
                val claims = jwtService.parseClaims(token)
                val userId = UUID.fromString(claims.subject)
                val role = claims["role"] as? String

                val userExists = userRepository.existsByIdAndIsDeletedFalse(userId)
                if (!userExists) {
                    SecurityContextHolder.clearContext()
                    chain.doFilter(req, res)
                    return
                }

                MDC.put(LogFields.USER_ID, userId.toString())

                val principal = AuthUser(userId, role)

                val authorities =
                    if (role == "admin")
                        listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
                    else
                        emptyList()


                val auth = UsernamePasswordAuthenticationToken(principal, null, authorities)
                auth.details = WebAuthenticationDetailsSource().buildDetails(req)
                SecurityContextHolder.getContext().authentication = auth
            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
                log.warn(
                    LogEvents.AUTH_JWT_INVALID + " {} {}",
                    StructuredArguments.kv(LogFields.URI_PATH, req.requestURI),
                    StructuredArguments.kv(LogFields.AUTH_FAILURE_REASON, e.javaClass.simpleName)
                )
            }
        }
        try {
            chain.doFilter(req, res)
        } finally {
            MDC.remove(LogFields.USER_ID)
        }
    }
}