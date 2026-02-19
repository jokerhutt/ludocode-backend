package com.ludocode.ludocodebackend.auth.infra.http.firebase

import com.google.firebase.auth.FirebaseAuth
import com.ludocode.ludocodebackend.auth.app.port.out.FirebaseAuthPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!test")
@Component
class FirebaseAuthAdapter(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthPort {

    override fun verifyIdToken(token: String): FirebaseUser {
        val decoded = firebaseAuth.verifyIdToken(token)
        val role = decoded.claims["role"] as? String
        return FirebaseUser(
            uid = decoded.uid,
            email = decoded.email,
            name = decoded.name,
            picture = decoded.picture,
            role = role
        )
    }
}