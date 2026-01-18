package com.ludocode.ludocodebackend.features.api.dto.response

import com.ludocode.ludocodebackend.storage.configuration.StorageProperties

data class ActiveFeaturesResponse(
    val storageMode: StorageProperties.Mode,
    val isAIEnabled: Boolean,
    val isPistonEnabled: Boolean,
    val isDemoEnabled: Boolean,
    val isAdminEnabled: Boolean
)