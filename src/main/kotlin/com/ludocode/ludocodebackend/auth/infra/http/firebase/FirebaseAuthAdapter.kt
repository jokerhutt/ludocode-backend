package com.ludocode.ludocodebackend.auth.infra.http.firebase

import com.google.firebase.auth.FirebaseAuth
import com.ludocode.ludocodebackend.auth.app.port.out.FirebaseAuthPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "firebase",
    name = ["enabled"],
    havingValue = "true"
)
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

    override fun deleteUser(uid: String) {
        firebaseAuth.deleteUser(uid)
    }

}