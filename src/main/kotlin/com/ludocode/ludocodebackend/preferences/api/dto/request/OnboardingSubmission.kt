package com.ludocode.ludocodebackend.preferences.api.dto.request

import java.util.UUID

data class OnboardingSubmission(
    val chosenPath: String,
    val chosenCourse: UUID,
    val hasProgrammingExperience: Boolean,
    var selectedUsername: String,
)