package com.ludocode.ludocodebackend.auth.app.port.out

import com.ludocode.ludocodebackend.auth.infra.http.firebase.FirebaseUser

interface FirebaseAuthPort {
    fun verifyIdToken(token: String): FirebaseUser
    fun deleteUser(uid: String)

}