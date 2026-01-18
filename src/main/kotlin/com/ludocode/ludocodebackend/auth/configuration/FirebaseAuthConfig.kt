package com.ludocode.ludocodebackend.auth.configuration

import com.google.firebase.auth.FirebaseAuth
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
@DependsOn("firebaseConfig")
class FirebaseAuthConfig (
    firebaseConfig: FirebaseConfig
){

    @Bean
    fun firebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()
}