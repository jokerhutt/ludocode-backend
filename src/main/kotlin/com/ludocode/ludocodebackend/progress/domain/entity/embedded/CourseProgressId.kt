package com.ludocode.ludocodebackend.progress.domain.entity.embedded

import java.util.UUID

data class CourseProgressId(
    val userId: UUID,
    val courseId: UUID
)
