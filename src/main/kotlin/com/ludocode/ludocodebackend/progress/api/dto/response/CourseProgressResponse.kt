package com.ludocode.ludocodebackend.progress.api.dto.response

import java.time.Instant
import java.util.*

data class CourseProgressResponse(
    val userId: UUID, val courseId: UUID,
    val moduleId: UUID, val id: UUID, val updatedAt: Instant
)
