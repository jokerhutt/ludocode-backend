package com.ludocode.ludocodebackend.preferences.api.dto
import java.util.UUID

data class OnboardingFormResponse(
    val courses: List<CourseOption>,
    val careers: List<CareerOption>,
    val experienceOptions: List<ExperienceOption>
)

data class CourseOption(
    val courseId: UUID,
    val title: String,
    val description: String
)

data class CareerOption(
    val id: UUID,
    val title: String,
    val description: String,
    val defaultCourseId: UUID
)

data class ExperienceOption(
    val value: Boolean,
    val content: String
)