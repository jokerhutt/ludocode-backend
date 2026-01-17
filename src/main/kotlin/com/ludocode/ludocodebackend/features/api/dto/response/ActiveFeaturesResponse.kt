package com.ludocode.ludocodebackend.features.api.dto.response

data class ActiveFeaturesResponse(
    val isAIEnabled: Boolean,
    val isPistonEnabled: Boolean,
    val isDemoEnabled: Boolean,
    val isAdminEnabled: Boolean
)