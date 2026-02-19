package com.ludocode.ludocodebackend.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@TestConfiguration
@Profile("test")
class TestSecurityConfig {
    @Bean
    fun testFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .addFilterBefore(HeaderAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    private object HeaderAuthFilter : OncePerRequestFilter() {
        override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
            val header = req.getHeader("X-Test-User-Id")
            if (header != null && SecurityContextHolder.getContext().authentication == null) {
                val uid = runCatching { UUID.fromString(header) }.getOrNull()
                if (uid != null) {
                    val auth = UsernamePasswordAuthenticationToken(
                        AuthUser(uid),
                        null,
                        emptyList()
                    )
                    auth.details = WebAuthenticationDetailsSource().buildDetails(req)
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
            chain.doFilter(req, res)
        }
    }
}