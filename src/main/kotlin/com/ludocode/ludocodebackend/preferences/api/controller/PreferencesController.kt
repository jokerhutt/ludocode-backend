package com.ludocode.ludocodebackend.preferences.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.preferences.api.dto.response.CareerResponse
import com.ludocode.ludocodebackend.preferences.api.dto.request.TogglePreferencesRequest
import com.ludocode.ludocodebackend.preferences.app.service.PreferencesService
import com.ludocode.ludocodebackend.preferences.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.preferences.domain.entity.UserPreferences
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(
    name = "Preferences",
    description = "Operations related to user's preference settings"
)
@RestController
@RequestMapping(ApiPaths.PREFERENCES.BASE)
class PreferencesController(private val preferencesService: PreferencesService) {

    @Operation(
        summary = "Complete user preferences",
        description = """
        Completes the preferences flow for the currently authenticated user.
        Stores the user's selected learning path + preferences and initializes their course progress.
        Returns the updated user profile and preferences state.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping
    fun submitOnboarding(
        @RequestBody req: OnboardingSubmission,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<OnboardingResponse> {
        return ResponseEntity.ok(preferencesService.createPreferences(req, userId))
    }


    @Operation(
        summary = "Get all available careers",
        description = """
        Returns a list of all available career preferences.
        Includes basic career metadata such as ID and title. 
        """
    )
    @GetMapping(ApiPaths.PREFERENCES.CAREERS)
    fun getCareerChoices() : ResponseEntity<List<CareerResponse>> {
        return ResponseEntity.ok(preferencesService.getCareerPreferences())
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
    @PatchMapping
    fun updateTogglePreferences(
        @RequestBody req: TogglePreferencesRequest,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<UserPreferences> {
        return ResponseEntity.ok(preferencesService.updatePreference(userId, req))
    }

    @Operation(
        summary = "Get current user's preferences",
        description = """
        Returns the preference settings for the currently authenticated user.
        Requires a valid session cookie. 
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    fun getUserPreferences(@AuthenticationPrincipal(expression = "userId") userId: UUID): ResponseEntity<UserPreferences> {
        return ResponseEntity.ok(preferencesService.getPreferences(userId))
    }


}