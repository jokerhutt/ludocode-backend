package com.ludocode.ludocodebackend.preferences.api.dto

import java.util.UUID

data class OnboardingDraftResponse(
    val username: String?,
    val careerId: UUID?,
    val courseId: UUID?,
    val hasExperience: Boolean?
)