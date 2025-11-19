package com.ludocode.ludocodebackend.progress.api.controller.internal

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserCoinsUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(InternalPathConstants.ICOINSPROGRESS)
class InternalUserCoinsController (
    private val userCoinsUseCase: UserCoinsUseCase
) {

    @PostMapping(InternalPathConstants.ICOINSUPSERT)
    fun findOrCreateStats(@PathVariable userId: UUID) : ResponseEntity<UserCoinsResponse> {
        return ResponseEntity.ok(userCoinsUseCase.findOrCreateCoins(userId))
    }


}