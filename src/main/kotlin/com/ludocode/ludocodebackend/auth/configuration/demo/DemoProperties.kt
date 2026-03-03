package com.ludocode.ludocodebackend.auth.configuration.demo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.UUID

@ConfigurationProperties(prefix = "demo")
@Component
class DemoProperties {
    var userId: UUID = UUID.fromString("598ccbea-4957-4569-81cb-ea901b62c329")
}