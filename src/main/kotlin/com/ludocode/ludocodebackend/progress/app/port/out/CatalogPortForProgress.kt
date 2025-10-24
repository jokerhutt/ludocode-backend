package com.ludocode.ludocodebackend.progress.app.port.out

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.infra.projection.LessonIdTreeProjection
import java.util.UUID

interface CatalogPortForProgress {
    fun findFirstLessonIdInCourse(courseId: UUID): UUID?
    fun findModuleIdForLesson(lessonId: UUID): UUID?
    fun findCourseIdForLesson(lessonId: UUID): UUID?
    fun findNextLessonId(lessonId: UUID): UUID?
    fun findLessonIdTree(lessonId: UUID): LessonTreeWithIdDTO?
    fun findLessonResponseById(lessonId: UUID, userId: UUID): LessonResponse
}