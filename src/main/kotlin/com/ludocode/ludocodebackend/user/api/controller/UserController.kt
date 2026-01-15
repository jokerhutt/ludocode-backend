package com.ludocode.ludocodebackend.user.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.user.api.dto.request.EditProfileRequest
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.AvatarInfo
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.service.UserService
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
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

    @GetMapping
    fun getUsersByIds(@RequestParam userIds: List<UUID>) : ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userService.getUsersByIds(userIds))
    }

    @PutMapping(ApiPaths.USERS.ONBOARDING)
    fun submitOnboarding(@RequestBody req: OnboardingSubmission, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<OnboardingResponse> {
        return ResponseEntity.ok(userService.createPreferences(req,userId))
    }

    @PutMapping(ApiPaths.USERS.AVATAR)
    fun changeUserAvatar(@RequestBody req: AvatarInfo, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.changeUserAvatar(userId, req))
    }

    @PutMapping(ApiPaths.USERS.ME)
    fun editUserProfile(@RequestBody req: EditProfileRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.editUser(userId, req))
    }

    @DeleteMapping(ApiPaths.USERS.ME)
    fun deleteUser(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping(ApiPaths.USERS.PREFERENCES)
    fun getUserPreferences(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserPreferences> {
        return ResponseEntity.ok(userService.getPreferences(userId))
    }

}