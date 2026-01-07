package com.ludocode.ludocodebackend.auth.api.dto

data class FirebaseUser(
    val uid: String,
    val email: String?,
    val name: String?,
    val picture: String?
)