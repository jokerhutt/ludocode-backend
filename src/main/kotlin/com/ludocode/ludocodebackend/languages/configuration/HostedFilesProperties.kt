package com.ludocode.ludocodebackend.languages.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("hosted")
@Component
class HostedFilesProperties {
    var enabled: Boolean = false
        var url: String = ""
}