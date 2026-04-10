package com.ludocode.ludocodebackend.progress.api.controller
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.api.dto.response.UserXpResponse
import com.ludocode.ludocodebackend.progress.app.service.UserXpService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(
    name = "XP",
    description = "Operations related to a users XP"
)
@RestController
@RequestMapping(ApiPaths.PROGRESS.COINS.BASE)
class UserXpController(private val userXpService: UserXpService) {

    @Operation(summary = "Get xp by user IDs", description = "Returns xp amounts for the specified user IDs.")
    @GetMapping
    fun getStatsListByUserIds(@RequestParam userIds: List<UUID>): ResponseEntity<List<UserXpResponse>> {
        return ResponseEntity.ok(userXpService.getUserXpList(userIds))
    }


}