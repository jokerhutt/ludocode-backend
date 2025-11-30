package com.ludocode.ludocodebackend.progress.api.controller

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.app.service.StreakService
import com.ludocode.ludocodebackend.progress.dto.response.DailyGoalResponse
import com.ludocode.ludocodebackend.progress.dto.response.UserStreakResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.PROGRESS_STREAK)
class StreakController(private val streakService: StreakService) {

    @GetMapping(PathConstants.GET_STREAK)
    fun getUserStreak (@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UserStreakResponse> {
        return ResponseEntity.ok(streakService.getStreak(userId))
    }

    @GetMapping(PathConstants.GET_STREAK_WEEK)
    fun getStreakPastWeek(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<DailyGoalResponse>> {
        return ResponseEntity.ok(streakService.getPastWeekMondayToSunday(userId))
    }

}