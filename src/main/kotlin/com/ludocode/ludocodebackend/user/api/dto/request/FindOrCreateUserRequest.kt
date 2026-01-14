package com.ludocode.ludocodebackend.user.api.dto.request

import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider


data class FindOrCreateUserRequest(
    val provider: AuthProvider,
    val providerUserId: String,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?
)
