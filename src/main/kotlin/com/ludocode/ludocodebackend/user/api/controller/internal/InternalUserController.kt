package com.ludocode.ludocodebackend.user.api.controller.internal

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.port.`in`.UserUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(InternalPathConstants.IUSERS)
class InternalUserController(
    private val userUseCase: UserUseCase
) {

    @PostMapping(InternalPathConstants.IUSERS_FIND_CREATE)
    fun findOrCreate(@RequestBody req: FindOrCreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userUseCase.findOrCreate(req))
    }

    @GetMapping(InternalPathConstants.IUSER_ID)
    fun getById(id: UUID) : ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userUseCase.getById(id))
    }


}