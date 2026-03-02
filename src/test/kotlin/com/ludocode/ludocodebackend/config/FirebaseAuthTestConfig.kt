package com.ludocode.ludocodebackend.config

import com.ludocode.ludocodebackend.auth.app.port.out.FirebaseAuthPort
import com.ludocode.ludocodebackend.auth.infra.http.firebase.FirebaseUser
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class FirebaseAuthTestConfig {

    @Bean
    @Primary
    fun firebaseAuthPort(): FirebaseAuthPort =
        object : FirebaseAuthPort {

            override fun verifyIdToken(token: String): FirebaseUser {
                return FirebaseUser(
                    uid = MockOauthConstants.USER_1_GOOGLE_SUB,
                    email = "email@google.com",
                    name = "John Doe",
                    picture = "https://example.com/avatar.png",
                    role = null
                )
            }


            override fun deleteUser(uid: String) {
            }

        }
}