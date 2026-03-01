package com.ludocode.ludocodebackend.auth.configuration.demo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.UUID

@ConfigurationProperties(prefix = "demo")
@Component
class DemoProperties {
    var enabled: Boolean = false
    var grantAdmin: Boolean = false
    lateinit var token: String
    lateinit var userId: UUID
}