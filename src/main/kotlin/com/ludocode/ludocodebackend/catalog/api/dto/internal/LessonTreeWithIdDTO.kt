package com.ludocode.ludocodebackend.catalog.api.dto.internal

import java.util.UUID

data class LessonTreeWithIdDTO(
    val lessonId: UUID,
    val moduleId: UUID,
    val courseId: UUID,
    val nextLessonId: UUID?
)
