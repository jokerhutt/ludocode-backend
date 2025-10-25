package com.ludocode.ludocodebackend.progress.api.controller.external

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStatsResponse
import com.ludocode.ludocodebackend.progress.app.service.UserStatsService
import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.PROGRESS_STATS)
class UserStatsController(private val userStatsService: UserStatsService) {

    @GetMapping(PathConstants.STATS_BY_USER_ID)
    fun getStatsForUser (@PathVariable userId: UUID) : ResponseEntity<UserStats> {
        return ResponseEntity.ok(userStatsService.getUserStats(userId))
    }

    @GetMapping(PathConstants.STATS_BY_USER_IDS)
    fun getStatsListByUserIds (@RequestParam userIds: List<UUID>) : ResponseEntity<List<UserStatsResponse>> {
        return ResponseEntity.ok(userStatsService.getUserStatsList(userIds))
    }



}