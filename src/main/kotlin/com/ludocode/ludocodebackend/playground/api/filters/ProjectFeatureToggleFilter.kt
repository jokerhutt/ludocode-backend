package com.ludocode.ludocodebackend.playground.api.filters

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.playground.config.ProjectFeatureConfig
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class ProjectFeatureToggleFilter(
    private val projectFeatureConfig: ProjectFeatureConfig,
) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        if (!req.requestURI.startsWith("${PathConstants.PROJECT}")) {
            chain.doFilter(req, res)
            return
        }

        if (!projectFeatureConfig.enabled) {
            res.sendError(403, "GCS disabled — toggle gcs.enabled=true to enable")
            return
        }

        chain.doFilter(req, res)
    }


}