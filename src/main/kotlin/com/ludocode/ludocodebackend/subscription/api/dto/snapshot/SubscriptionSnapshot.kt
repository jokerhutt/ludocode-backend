package com.ludocode.ludocodebackend.subscription.api.dto.snapshot

import java.time.OffsetDateTime

data class StripeSubscriptionSnapshot(
    val priceId: String,
    val customerId: String,
    val subscriptionId: String,
    val periodStart: OffsetDateTime,
    val periodEnd: OffsetDateTime,
    val status: String
)