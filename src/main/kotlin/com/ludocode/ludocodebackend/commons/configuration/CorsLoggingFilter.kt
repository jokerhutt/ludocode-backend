package com.ludocode.ludocodebackend.commons.configuration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CorsLoggingFilter(private val corsProps: CorsProps) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(CorsLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = request.getHeader("Origin")

        if (origin != null) {
            val isAllowed = corsProps.origins.any { pattern ->
                matchesPattern(origin, pattern)
            }

            if (isAllowed) {
                log.debug("CORS request allowed - Origin: {} matches allowed patterns: {}",
                    origin, corsProps.origins)
            } else {
                log.warn("CORS request REJECTED - Origin: '{}' does not match any allowed patterns: {}. " +
                    "Request method: {}, Request URI: {}",
                    origin, corsProps.origins, request.method, request.requestURI)
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
