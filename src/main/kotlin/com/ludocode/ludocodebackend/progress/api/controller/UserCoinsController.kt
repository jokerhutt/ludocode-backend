package com.ludocode.ludocodebackend.progress.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.app.service.UserCoinsService
import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.PROGRESS.COINS.BASE)
class UserCoinsController(private val userCoinsService: UserCoinsService) {

    @GetMapping
    fun getStatsListByUserIds (@RequestParam userIds: List<UUID>) : ResponseEntity<List<UserCoinsResponse>> {
        return ResponseEntity.ok(userCoinsService.getUserCoinsList(userIds))
    }



}