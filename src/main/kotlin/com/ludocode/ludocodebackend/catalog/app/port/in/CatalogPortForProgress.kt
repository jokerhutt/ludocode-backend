package com.ludocode.ludocodebackend.catalog.app.port.`in`

import java.util.*

interface CatalogPortForProgress {
    fun findFirstModuleIdInCourse(courseId: UUID): UUID
    fun findModuleIdForLesson(lessonId: UUID): UUID
}