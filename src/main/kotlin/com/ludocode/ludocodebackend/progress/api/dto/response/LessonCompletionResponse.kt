package com.ludocode.ludocodebackend.progress.api.dto.response

import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse

data class LessonCompletionResponse(
    val newStats: UserStats,
    val newCourseProgress: CourseProgressResponse
)
