package com.ludocode.ludocodebackend.catalog.app.port.`in`

import java.util.UUID

interface CatalogUseCase {

    fun findFirstLessonIdInCourse(courseId: UUID): UUID?
    fun findModuleIdForLesson(lessonId: UUID): UUID?

}