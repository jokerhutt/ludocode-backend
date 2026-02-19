package com.ludocode.ludocodebackend.user.app.port.`in`

import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import java.util.*

interface UserPortForAuth {
    fun findOrCreate(req: FindOrCreateUserRequest): UserResponse
    fun getById(id: UUID): UserResponse
}