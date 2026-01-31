package com.ludocode.ludocodebackend.auth.api.security.principal

import java.util.UUID

data class AuthUser(val userId: UUID, val role: String?)