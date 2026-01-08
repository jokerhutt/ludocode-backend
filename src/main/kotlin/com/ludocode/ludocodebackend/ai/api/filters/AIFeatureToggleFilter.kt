package com.ludocode.ludocodebackend.ai.api.filters

import com.ludocode.ludocodebackend.ai.configuration.AIFeatureConfig
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AIFeatureToggleFilter(
    private val aiFeatureConfig: AIFeatureConfig
) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        if (!req.requestURI.startsWith("${ApiPaths.AI.BASE}")) {
            chain.doFilter(req, res)
            return
        }

        if (!req.requestURI.startsWith("${ApiPaths.CREDITS.BASE}")) {
            chain.doFilter(req, res)
            return
        }

        if (!aiFeatureConfig.enabled) {
            res.sendError(403, "AI disabled — toggle ai.enabled=true to enable")
            return
        }

        chain.doFilter(req, res)
    }
}