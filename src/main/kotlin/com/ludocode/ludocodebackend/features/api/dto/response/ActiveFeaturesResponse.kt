package com.ludocode.ludocodebackend.features.api.dto.response

import com.ludocode.ludocodebackend.storage.configuration.StorageProperties
import com.ludocode.ludocodebackend.subscription.configuration.StripeMode

data class ActiveFeaturesResponse(
    val storageMode: StorageProperties.Mode,
    val paymentsEnabled: Boolean,
    val stripeMode: StripeMode,
    val isAIEnabled: Boolean,
    val isPistonEnabled: Boolean,
    val isDemoEnabled: Boolean,
    val isAdminEnabled: Boolean
)