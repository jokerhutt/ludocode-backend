package com.ludocode.ludocodebackend.features.api.controller

import com.ludocode.ludocodebackend.ai.configuration.AIFeatureConfig
import com.ludocode.ludocodebackend.auth.configuration.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.features.api.dto.response.ActiveFeaturesResponse
import com.ludocode.ludocodebackend.playground.config.PistonFeatureConfig
import com.ludocode.ludocodebackend.storage.configuration.StorageProperties
import com.ludocode.ludocodebackend.subscription.configuration.StripeProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Features",
    description = "Operations related to querying feature toggle states"
)
@RestController
@RequestMapping(ApiPaths.FEATURES.BASE)
class FeaturesController(
    private val storageProps: StorageProperties,
    private val stripeProps: StripeProperties,
    private val aiConfig: AIFeatureConfig,
    private val pistonConfig: PistonFeatureConfig,
    private val demoConfig: DemoConfig,
    private val env: Environment
) {

    fun isAdminEnabled(): Boolean =
        env.activeProfiles.contains("admin") || env.activeProfiles.contains("devadmin") || env.activeProfiles.contains("devtestadmin")

    @Operation(
        summary = "Get feature toggle status",
        description = "Returns the enabled or disabled status of all runtime feature flags."
    )
    @GetMapping
    fun getActiveFeatures(): ResponseEntity<ActiveFeaturesResponse> {
        return ResponseEntity.ok(
            ActiveFeaturesResponse(
                storageMode = storageProps.mode,
                isAIEnabled = aiConfig.enabled,
                isPistonEnabled = pistonConfig.enabled,
                stripeMode = stripeProps.mode,
                paymentsEnabled = stripeProps.enabled,
                isDemoEnabled = demoConfig.enabled,
                isAdminEnabled = isAdminEnabled()
            )
        )
    }
}