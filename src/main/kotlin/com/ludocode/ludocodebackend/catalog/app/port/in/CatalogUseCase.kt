package com.ludocode.ludocodebackend.catalog.app.port.`in`

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.infra.projection.LessonIdTreeProjection
import java.util.UUID

interface CatalogUseCase {

    fun findFirstLessonIdInCourse(courseId: UUID): UUID
    fun findModuleIdForLesson(lessonId: UUID): UUID
    fun findNextLessonId(currentLessonId: UUID): UUID?
    fun findCourseIdForLesson(lessonId: UUID): UUID
    fun findLessonIdTree(lessonId: UUID) : LessonTreeWithIdDTO
    fun findLessonResponseById(lessonId: UUID, userId: UUID): LessonResponse

}