package com.ludocode.ludocodebackend.auth.configuration.firebase

import com.ludocode.ludocodebackend.auth.app.port.out.FirebaseAuthPort
import com.ludocode.ludocodebackend.auth.infra.http.firebase.FirebaseUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(
    prefix = "firebase",
    name = ["enabled"],
    havingValue = "false",
    matchIfMissing = true
)
class NoFirebaseAuthConfig {

    @Bean
    fun firebaseAuthPort(): FirebaseAuthPort =
        object : FirebaseAuthPort {
            override fun verifyIdToken(token: String): FirebaseUser {
                throw IllegalStateException("Firebase disabled")
            }

            override fun deleteUser(uid: String) {
            }
        }
}