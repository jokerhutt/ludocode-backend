package com.ludocode.ludocodebackend.subscription.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stripe")
data class StripeProperties(
    val enabled: Boolean,
    val mode: StripeMode,
    val secretKey: String,
    val webhookSecret: String
)

enum class StripeMode {
    PROD,
    DEV_UNLIMITED,
    FREE_ONLY
}