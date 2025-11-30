package com.ludocode.ludocodebackend.progress.dto.response

import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

data class CourseProgressResponse(
    val userId: UUID, val courseId: UUID, val currentLessonId: UUID,
    val moduleId: UUID, val id: UUID, val updatedAt: Instant
)
