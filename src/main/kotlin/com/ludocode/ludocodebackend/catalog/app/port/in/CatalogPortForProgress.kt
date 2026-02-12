package com.ludocode.ludocodebackend.catalog.app.port.`in`

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import java.util.UUID

interface CatalogPortForProgress {
    fun findFirstModuleIdInCourse(courseId: UUID): UUID
    fun findModuleIdForLesson(lessonId: UUID): UUID
    fun findLessonResponseById(lessonId: UUID, userId: UUID): LessonResponse
}