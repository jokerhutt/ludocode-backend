package com.ludocode.ludocodebackend.auth.api.filters

import com.ludocode.ludocodebackend.auth.configuration.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class DemoFeatureToggleFilter(
    private val demoConfig: DemoConfig
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {

        if (!request.requestURI.startsWith(ApiPaths.AUTH.DEMO)) {
            chain.doFilter(request, response)
            return
        }

        if (!demoConfig.enabled) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Demo disabled, to enable it edit the demo.enabled flag in application-yml")
            return
        }


        chain.doFilter(request, response)
    }
}