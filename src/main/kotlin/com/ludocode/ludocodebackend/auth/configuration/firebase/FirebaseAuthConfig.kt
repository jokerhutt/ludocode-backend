package com.ludocode.ludocodebackend.auth.configuration.firebase

import com.google.firebase.auth.FirebaseAuth
import com.ludocode.ludocodebackend.auth.configuration.firebase.FirebaseConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@Configuration
@ConditionalOnProperty(
    prefix = "firebase",
    name = ["enabled"],
    havingValue = "true"
)
@DependsOn("firebaseConfig")
class FirebaseAuthConfig(
    firebaseConfig: FirebaseConfig
) {

    @Bean
    fun firebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()
}