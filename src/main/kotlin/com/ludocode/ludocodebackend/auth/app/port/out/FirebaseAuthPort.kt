package com.ludocode.ludocodebackend.auth.app.port.out

import com.ludocode.ludocodebackend.auth.api.dto.FirebaseUser

interface FirebaseAuthPort {
    fun verifyIdToken(token: String): FirebaseUser
}