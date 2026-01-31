package com.ludocode.ludocodebackend.auth.infra.http.firebase

data class FirebaseUser(
    val uid: String,
    val email: String?,
    val name: String?,
    val picture: String?,
    val role: String?
)