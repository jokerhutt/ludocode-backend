package com.ludocode.ludocodebackend.auth.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

@ConfigurationProperties(prefix = "demo")
@Component
class DemoConfig {
    var enabled: Boolean = false
    lateinit var token: String
    lateinit var userId: UUID
}