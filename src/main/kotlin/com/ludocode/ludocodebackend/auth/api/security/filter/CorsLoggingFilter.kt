package com.ludocode.ludocodebackend.auth.api.security.filter

import com.ludocode.ludocodebackend.commons.configuration.web.CorsProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CorsLoggingFilter(private val corsProperties: CorsProperties) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(CorsLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = request.getHeader("Origin")

        if (origin != null) {
            val isAllowed = corsProperties.origins.any { pattern ->
                matchesPattern(origin, pattern)
            }

            if (isAllowed) {
                log.debug(
                    "CORS request allowed - Origin: {} matches allowed patterns: {}",
                    origin, corsProperties.origins
                )
            } else {
                log.warn(
                    "CORS request REJECTED - Origin: '{}' does not match any allowed patterns: {}. " +
                            "Request method: {}, Request URI: {}",
                    origin, corsProperties.origins, request.method, request.requestURI
                )
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun matchesPattern(origin: String, pattern: String): Boolean {
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .toRegex()
        return regex.matches(origin)
    }
}