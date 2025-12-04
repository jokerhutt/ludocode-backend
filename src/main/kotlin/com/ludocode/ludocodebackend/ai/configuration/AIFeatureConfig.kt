package com.ludocode.ludocodebackend.ai.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("ai")
@Component
class AIFeatureConfig {
    var enabled: Boolean = true
}