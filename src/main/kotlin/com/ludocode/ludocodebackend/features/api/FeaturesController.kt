package com.ludocode.ludocodebackend.features.api
import org.springframework.core.env.Environment
import com.ludocode.ludocodebackend.ai.configuration.AIFeatureConfig
import com.ludocode.ludocodebackend.auth.configuration.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.features.api.dto.response.ActiveFeaturesResponse
import com.ludocode.ludocodebackend.playground.config.GcsFeatureConfig
import com.ludocode.ludocodebackend.playground.config.PistonFeatureConfig
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.FEATURES.BASE)
class FeaturesController(
    private val aiConfig: AIFeatureConfig,
    private val gcsConfig: GcsFeatureConfig,
    private val pistonConfig: PistonFeatureConfig,
    private val demoConfig: DemoConfig,
    private val env: Environment
) {

    fun isAdminEnabled(): Boolean =
        env.activeProfiles.contains("admin")

    @GetMapping
    fun getActiveFeatures(): ResponseEntity<ActiveFeaturesResponse> {
        return ResponseEntity.ok(
            ActiveFeaturesResponse(
                isAIEnabled = aiConfig.enabled,
                isGcsEnabled = gcsConfig.enabled,
                isPistonEnabled = pistonConfig.enabled,
                isDemoEnabled = demoConfig.enabled,
                isAdminEnabled = isAdminEnabled()
            )
        )
    }
}