package com.ludocode.ludocodebackend.commons.configuration.web
import com.fasterxml.jackson.databind.ObjectMapper
import com.ludocode.ludocodebackend.commons.configuration.app.MaintenanceProperties
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
@Component
@Order(1)
class MaintenanceFilter(
    private val maintenanceProperties: MaintenanceProperties,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (maintenanceProperties.enabled && request.requestURI != ApiPaths.MAINTENANCE.BASE) {
            response.status = HttpStatus.SERVICE_UNAVAILABLE.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            objectMapper.writeValue(
                response.writer,
                mapOf("status" to 503, "message" to "Service is temporarily unavailable for maintenance. Please try again later.")
            )
            return
        }
        filterChain.doFilter(request, response)
    }
}
