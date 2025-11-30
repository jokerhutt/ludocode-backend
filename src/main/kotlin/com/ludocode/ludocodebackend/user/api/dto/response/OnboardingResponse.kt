package com.ludocode.ludocodebackend.user.api.dto.response

import com.ludocode.ludocodebackend.progress.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import java.util.UUID

data class OnboardingResponse(val preferences: UserPreferences, val courseProgressResponse: CourseProgressResponseWithEnrolled)
