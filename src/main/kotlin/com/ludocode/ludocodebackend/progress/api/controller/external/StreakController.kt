package com.ludocode.ludocodebackend.progress.api.controller.external

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.app.service.StreakService
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.STREAK)
class StreakController(private val streakService: StreakService) {

    @GetMapping(PathConstants.GET_STREAK)
    fun getUserStreak (@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserStreakResponse> {
        return ResponseEntity.ok(streakService.getStreak(userId))
    }

}