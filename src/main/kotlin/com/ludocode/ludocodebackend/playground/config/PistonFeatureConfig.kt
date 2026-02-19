package com.ludocode.ludocodebackend.playground.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("piston")
@Component
class PistonFeatureConfig {
    var enabled: Boolean = true
}