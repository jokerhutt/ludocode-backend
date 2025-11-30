package com.ludocode.ludocodebackend.playground.api.filters

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.playground.config.PistonFeatureConfig
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class PistonFeatureToggleFilter(
    private val pistonFeatureConfig: PistonFeatureConfig
) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        if (!req.requestURI.startsWith("${PathConstants.RUNNER}")) {
            chain.doFilter(req, res)
            return
        }

        if (!pistonFeatureConfig.enabled) {
            res.sendError(403, "Piston disabled — toggle piston.enabled=true to enable")
            return
        }

        chain.doFilter(req, res)
    }
}