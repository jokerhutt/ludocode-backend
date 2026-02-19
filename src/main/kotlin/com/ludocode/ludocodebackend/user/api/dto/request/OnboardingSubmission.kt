package com.ludocode.ludocodebackend.user.api.dto.request

import com.ludocode.ludocodebackend.user.domain.enums.DesiredPath
import java.util.*

data class OnboardingSubmission(
    val chosenPath: DesiredPath,
    val chosenCourse: UUID,
    val hasProgrammingExperience: Boolean,
    val selectedUsername: String,
)

