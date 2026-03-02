package com.ludocode.ludocodebackend.auth.configuration.firebase

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "firebase")
@Component
class FirebaseProperties {
    var enabled: Boolean = false
}

