package com.ludocode.ludocodebackend.auth.app.port.out

import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import java.util.UUID

interface UserPortForAuth {
    fun findOrCreate(req: FindOrCreateUserRequest): UserResponse
    fun getById(id: UUID): UserResponse
}