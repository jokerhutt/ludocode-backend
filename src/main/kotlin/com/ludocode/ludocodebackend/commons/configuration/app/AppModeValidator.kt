package com.ludocode.ludocodebackend.commons.configuration.app

import com.google.api.client.util.Value
import com.ludocode.ludocodebackend.auth.configuration.firebase.FirebaseProperties
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class AppModeValidator(
    private val appProperties: AppProperties,
    private val firebaseProperties: FirebaseProperties
) {

    @PostConstruct
    fun validate() {
        if (!firebaseProperties.enabled && appProperties.openToPublic) {
            throw IllegalStateException(
                "OPEN_TO_PUBLIC cannot be true when FIREBASE_ENABLED is false"
            )
        }
    }
}