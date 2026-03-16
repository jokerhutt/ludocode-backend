package com.ludocode.ludocodebackend.auth.api.security.filter

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.configuration.web.CorsProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.logstash.logback.argument.StructuredArguments.kv
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
                    LogEvents.CORS_REQUEST_ALLOWED + " {} {} {}",
                    kv(LogFields.ORIGIN, origin),
                    kv(LogFields.ALLOWED_ORIGINS_COUNT, corsProperties.origins.size),
                    kv(LogFields.URI_PATH, request.requestURI)
                )
            } else {
                log.warn(
                    LogEvents.CORS_REQUEST_REJECTED + " {} {} {} {}",
                    kv(LogFields.ORIGIN, origin),
                    kv(LogFields.ALLOWED_ORIGINS_COUNT, corsProperties.origins.size),
                    kv(LogFields.REQUEST_METHOD, request.method),
                    kv(LogFields.URI_PATH, request.requestURI)
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