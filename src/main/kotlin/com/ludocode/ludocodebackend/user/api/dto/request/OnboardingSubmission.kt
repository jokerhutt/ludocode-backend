package com.ludocode.ludocodebackend.user.api.dto.request

import com.ludocode.ludocodebackend.preferences.domain.enums.DesiredPath
import java.util.*

data class OnboardingSubmission(
    val chosenPath: String,
    val chosenCourse: UUID,
    val hasProgrammingExperience: Boolean,
    val selectedUsername: String,
)

