package com.ludocode.ludocodebackend.user.api.dto.request

import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import java.util.UUID


data class FindOrCreateUserRequest(
    val provider: AuthProvider,
    val providerUserId: String,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val name: String?,
    val avatarUrl: String?
)
