package com.ludocode.ludocodebackend.user.domain.event

import java.util.UUID

data class UserRegisteredEvent(
    val userId: UUID
)