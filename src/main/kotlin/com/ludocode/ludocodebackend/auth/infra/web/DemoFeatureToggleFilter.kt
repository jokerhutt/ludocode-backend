package com.ludocode.ludocodebackend.auth.infra.web

import com.ludocode.ludocodebackend.auth.config.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.PathConstants
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

        if (!request.requestURI.startsWith("${PathConstants.AUTH}${ PathConstants.DEMO_LOGIN }")) {
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