package com.ludocode.ludocodebackend.user.api.dto.request

import com.ludocode.ludocodebackend.user.domain.enums.DesiredPath
import java.util.UUID

data class OnboardingSubmission(
    val chosenPath: DesiredPath,
    val chosenCourse: UUID,
    val hasProgrammingExperience: Boolean
)

