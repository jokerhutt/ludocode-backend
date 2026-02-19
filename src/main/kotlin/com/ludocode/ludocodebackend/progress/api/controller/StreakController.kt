package com.ludocode.ludocodebackend.progress.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.progress.app.service.StreakService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Tag(
    name = "Streak",
    description = "Operations related to a users streak"
)
@SecurityRequirement(name = "sessionAuth")
@RestController
@RequestMapping(ApiPaths.PROGRESS.STREAK.BASE)
class StreakController(private val streakService: StreakService) {

    @Operation(
        summary = "Get user's streak information",
        description = """
        Returns streak information for the currently authenticated user.
        An optional mode parameter can be used to control the streak calculation
        (for example: daily or weekly). 
        """
    )
    @GetMapping
    fun getUserStreak(
        @RequestParam(required = false) mode: String?,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<*> {
        return when (mode) {
            "weekly" ->
                ResponseEntity.ok(streakService.getPastWeekMondayToSunday(userId))

            null ->
                ResponseEntity.ok(streakService.getStreak(userId))

            else ->
                throw ApiException(ErrorCode.BAD_REQ, "Invalid mode: $mode")
        }
    }

}