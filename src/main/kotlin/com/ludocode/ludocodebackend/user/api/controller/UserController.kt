package com.ludocode.ludocodebackend.user.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.user.api.dto.request.EditProfileRequest
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.AvatarInfo
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.service.UserService
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.USERS.BASE)
class UserController(private val userService: UserService) {

    @Operation(summary = "Get users by ID list", description = "Returns user profiles for the provided list of user IDs.")
    @GetMapping
    fun getUsersByIds(@RequestParam userIds: List<UUID>) : ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userService.getUsersByIds(userIds))
    }

    @Operation(
        summary = "Complete user onboarding",
        description = """
        Completes the onboarding flow for the currently authenticated user.
        Stores the user's selected learning path + preferences and initializes their course progress.
        Returns the updated user profile and onboarding state.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping(ApiPaths.USERS.ONBOARDING)
    fun submitOnboarding(@RequestBody req: OnboardingSubmission, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<OnboardingResponse> {
        return ResponseEntity.ok(userService.createPreferences(req,userId))
    }

    @Operation(
        summary = "Update user avatar",
        description = """
        Updates the avatar configuration for the currently authenticated user.
        Stores the selected avatar version and index.
        Returns the updated user profile. 
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping(ApiPaths.USERS.AVATAR)
    fun changeUserAvatar(@RequestBody req: AvatarInfo, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.changeUserAvatar(userId, req))
    }

    @Operation(
        summary = "Update current user profile",
        description = """
        Updates the currently authenticated user's display name and/or avatar index and returns the updated user.
        Requires a valid session cookie to be present.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping(ApiPaths.USERS.ME)
    fun editUserProfile(@RequestBody req: EditProfileRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.editUser(userId, req))
    }

    @Operation(
        summary = "Delete current user account",
        description = """
       Deletes the currently authenticated user's account.
        This operation removes the user from the system and invalidates the active session.
        Requires a valid session cookie to be present. 
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping(ApiPaths.USERS.ME)
    fun deleteUser(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary="Get current user's preferences",
        description="""
        Returns the preference settings for the currently authenticated user.
        Requires a valid session cookie. 
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping(ApiPaths.USERS.PREFERENCES)
    fun getUserPreferences(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserPreferences> {
        return ResponseEntity.ok(userService.getPreferences(userId))
    }

}