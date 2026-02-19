package com.ludocode.ludocodebackend.auth.configuration

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.ByteArrayInputStream

@Profile("!test")
@Configuration
class FirebaseConfig(
    @Value("\${firebase.service.json}")
    private val serviceAccountJson: String
) {

    @PostConstruct
    fun init() {
        val serviceAccountStream =
            ByteArrayInputStream(serviceAccountJson.toByteArray(Charsets.UTF_8))

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }
}