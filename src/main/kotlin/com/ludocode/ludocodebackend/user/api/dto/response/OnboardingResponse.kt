package com.ludocode.ludocodebackend.user.api.dto.response

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences

data class OnboardingResponse(val refreshedUser: UserResponse, val preferences: UserPreferences, val courseProgressResponse: CourseProgressResponseWithEnrolled)
