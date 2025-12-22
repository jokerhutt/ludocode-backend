package com.ludocode.ludocodebackend.user.api.controller

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.service.UserService
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.USERS)
class UserController(private val userService: UserService) {

    @GetMapping(PathConstants.USERS_FROM_IDS)
    fun getUsersByIds(@RequestParam userIds: List<UUID>) : ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userService.getUsersByIds(userIds))
    }

    @PostMapping(PathConstants.SUBMIT_ONBOARDING)
    fun submitOnboarding(@RequestBody req: OnboardingSubmission, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<OnboardingResponse> {
        return ResponseEntity.ok(userService.createPreferences(req,userId))
    }

    @GetMapping(PathConstants.PREFERENCES)
    fun getUserPreferences(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserPreferences> {
        return ResponseEntity.ok(userService.getPreferences(userId))
    }

}