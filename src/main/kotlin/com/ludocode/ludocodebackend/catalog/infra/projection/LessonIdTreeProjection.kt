package com.ludocode.ludocodebackend.catalog.infra.projection

import java.util.UUID

interface LessonIdTreeProjection {
    val lessonId: UUID
    val moduleId: UUID
    val courseId: UUID
    val nextLessonId: UUID?
}