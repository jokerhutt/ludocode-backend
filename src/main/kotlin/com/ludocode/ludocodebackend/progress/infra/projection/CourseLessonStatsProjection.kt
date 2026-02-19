package com.ludocode.ludocodebackend.progress.infra.projection

import java.util.*

interface CourseLessonStatsProjection {
    val courseId: UUID
    val totalLessons: Long
    val completedLessons: Long
}