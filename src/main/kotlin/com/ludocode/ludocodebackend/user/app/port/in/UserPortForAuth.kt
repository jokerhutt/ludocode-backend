package com.ludocode.ludocodebackend.user.app.port.`in`

import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import java.util.UUID

interface UserPortForAuth {
    fun findOrCreate(req: FindOrCreateUserRequest): UserResponse
    fun getById(id: UUID): UserResponse
    fun assertEmailAvailableForProvider(email: String, provider: AuthProvider)
}