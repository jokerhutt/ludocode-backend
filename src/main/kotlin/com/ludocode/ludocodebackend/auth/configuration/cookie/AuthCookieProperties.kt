package com.ludocode.ludocodebackend.auth.configuration.cookie

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "auth.cookie")
@Component
class AuthCookieProperties {

    lateinit var name: String
    lateinit var path: String
    lateinit var domain: String
    lateinit var sameSite: String

    var secure: Boolean = false
    var maxAgeSeconds: Long = 0
}