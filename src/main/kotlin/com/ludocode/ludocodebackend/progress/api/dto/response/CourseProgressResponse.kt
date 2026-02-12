package com.ludocode.ludocodebackend.progress.api.dto.response

import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

data class CourseProgressResponse(
    val userId: UUID, val courseId: UUID,
    val moduleId: UUID, val id: UUID, val updatedAt: Instant
)
