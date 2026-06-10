package com.ludocode.ludocodebackend.leaderboard.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.leaderboard.api.dto.WeeklyLeaderboardResponse
import com.ludocode.ludocodebackend.leaderboard.app.service.LeaderboardService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Leaderboard",
    description = "Operations related to querying weekly leaderboard"
)
@RestController
@RequestMapping(ApiPaths.LEADERBOARD.BASE)
class LeaderboardController(private val leaderboardService: LeaderboardService) {


    @GetMapping
    fun getWeeklyLeaderboard(): ResponseEntity<WeeklyLeaderboardResponse> {
       return ResponseEntity.ok(leaderboardService.getWeeklyLeaderboardStats())
    }


}