package com.ludocode.ludocodebackend.auth.configuration

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

@Configuration
class FirebaseConfig (
    @Value("\${firebase.service.path}")
    private val serviceAccountPath: String
){

    @PostConstruct
    fun init() {
        val serviceAccount =
            this::class.java.classLoader
                .getResourceAsStream(serviceAccountPath)
                ?: throw IllegalStateException("firebase-service-account.json not found on classpath")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }
}