package com.ludocode.ludocodebackend.runner.api.filters

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.runner.configuration.PistonProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class PistonFeatureToggleFilter(
    private val pistonProperties: PistonProperties
) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        if (!req.requestURI.startsWith(ApiPaths.RUNNER.WS_BASE)) {
            chain.doFilter(req, res)
            return
        }

        if (!pistonProperties.enabled) {
            res.sendError(403, "Piston disabled — toggle piston.enabled=true to enable")
            return
        }

        chain.doFilter(req, res)
    }
}