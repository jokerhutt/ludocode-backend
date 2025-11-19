package com.ludocode.ludocodebackend.progress.api.controller.external

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import com.ludocode.ludocodebackend.progress.app.service.UserCoinsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.PROGRESS_COINS)
class UserCoinsController(private val userCoinsService: UserCoinsService) {

    @GetMapping(PathConstants.COINS_BY_USER_IDS)
    fun getStatsListByUserIds (@RequestParam userIds: List<UUID>) : ResponseEntity<List<UserCoinsResponse>> {
        return ResponseEntity.ok(userCoinsService.getUserCoinsList(userIds))
    }



}