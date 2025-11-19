package com.ludocode.ludocodebackend.progress.api.controller.internal

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserStreakUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(InternalPathConstants.ISTREAKPROGRESS)
class InternalUserStreakController(private val userStreakUseCase: UserStreakUseCase) {

    @PostMapping(InternalPathConstants.ISTREAKUPSERT)
    fun upsertUserStreak(@PathVariable userId: UUID) : ResponseEntity<UserStreakResponse> {
        return ResponseEntity.ok(userStreakUseCase.getStreak(userId))
    }


}