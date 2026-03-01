package com.ludocode.ludocodebackend.runner.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("piston")
@Component
class PistonProperties {
    var enabled: Boolean = false
    var base: String = ""
    var public: String = "https://emkc.org/api/v2/piston"
}