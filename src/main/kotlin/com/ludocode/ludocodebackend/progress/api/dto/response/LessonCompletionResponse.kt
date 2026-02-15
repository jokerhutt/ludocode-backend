package com.ludocode.ludocodebackend.progress.api.dto.response

import com.ludocode.ludocodebackend.lesson.api.dto.response.LessonResponse
import java.math.BigDecimal

data class LessonCompletionResponse(
    val newCoins: UserCoinsResponse,
    val newStreak: UserStreakResponse,
    val newCourseProgress: CourseProgressResponse,
    val updatedCompletedLesson: LessonResponse,
    val accuracy: BigDecimal
)
