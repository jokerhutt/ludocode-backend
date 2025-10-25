package com.ludocode.ludocodebackend.progress.api.controller.internal

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStatsResponse
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserStatsUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(InternalPathConstants.ISTATSPROGRESS)
class InternalUserStatsController (
    private val userStatsUseCase: UserStatsUseCase
) {

    @PostMapping(InternalPathConstants.ISTATSUPSERT)
    fun findOrCreateStats(@PathVariable userId: UUID) : ResponseEntity<UserStatsResponse> {
        return ResponseEntity.ok(userStatsUseCase.findOrCreateStats(userId))
    }


}