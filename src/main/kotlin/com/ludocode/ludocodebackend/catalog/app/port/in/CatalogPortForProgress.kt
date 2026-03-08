package com.ludocode.ludocodebackend.catalog.app.port.`in`

import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import java.util.*

interface CatalogPortForProgress {
    fun findFirstModuleIdInCourse(courseId: UUID): UUID
    fun findModuleIdForLesson(lessonId: UUID): UUID
    fun findCourseById(courseId: UUID): Course
}