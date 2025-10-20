package com.ludocode.ludocodebackend.user.domain.entity

data class NewUser(
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?
)