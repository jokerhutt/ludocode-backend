package com.ludocode.ludocodebackend.subscription.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stripe")
data class StripeProperties(
    val enabled: Boolean,
    val secretKey: String,
    val webhookSecret: String
)