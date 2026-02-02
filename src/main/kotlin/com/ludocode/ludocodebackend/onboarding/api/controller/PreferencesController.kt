package com.ludocode.ludocodebackend.onboarding.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.onboarding.api.dto.TogglePreferencesRequest
import com.ludocode.ludocodebackend.onboarding.app.service.PreferencesService
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(
    name = "Preferences",
    description = "Operations related to user's preference settings"
)
@RestController
@RequestMapping(ApiPaths.PREFERENCES.BASE)
class PreferencesController(private val preferencesService: PreferencesService) {

    @Operation(
        summary = "Complete user onboarding",
        description = """
        Completes the onboarding flow for the currently authenticated user.
        Stores the user's selected learning path + preferences and initializes their course progress.
        Returns the updated user profile and onboarding state.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping
    fun submitOnboarding(@RequestBody req: OnboardingSubmission, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<OnboardingResponse> {
        return ResponseEntity.ok(preferencesService.createPreferences(req,userId))
    }

    @Operation(
        summary = "Update user toggle preferences",
        description = """
        Updates AI and Audio preferences for the currently authenticated user.
        Returns updated preference state.
        Requires a valid session cookie.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    fun updateTogglePreferences(@RequestBody req: TogglePreferencesRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserPreferences> {
        return ResponseEntity.ok(preferencesService.updateTogglePreferences(userId, req))
    }

    @Operation(
        summary="Get current user's preferences",
        description="""
        Returns the preference settings for the currently authenticated user.
        Requires a valid session cookie. 
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    fun getUserPreferences(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserPreferences> {
        return ResponseEntity.ok(preferencesService.getPreferences(userId))
    }


}