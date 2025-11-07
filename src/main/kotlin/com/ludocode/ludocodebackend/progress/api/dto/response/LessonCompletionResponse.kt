package com.ludocode.ludocodebackend.progress.api.dto.response

import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import java.math.BigDecimal

data class LessonCompletionResponse(
    val newStats: UserStatsResponse,
    val newStreak: UserStreakResponse,
    val newCourseProgress: CourseProgressResponse,
    val updatedCompletedLesson: LessonResponse,
    val accuracy: BigDecimal
)
