package com.ludocode.ludocodebackend.analytics.api.dto

import com.ludocode.ludocodebackend.analytics.domain.enums.AnalyticsEventKey

data class AnalyticsEventRequest(
    val event: AnalyticsEventKey,
    val properties: Map<String, Any> = emptyMap()
)
