package com.ludocode.ludocodebackend.user.domain.entity

import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import java.util.UUID

data class NewExternalAccount(
    val userId: UUID,
    val provider: AuthProvider,
    val providerUserId: String
)