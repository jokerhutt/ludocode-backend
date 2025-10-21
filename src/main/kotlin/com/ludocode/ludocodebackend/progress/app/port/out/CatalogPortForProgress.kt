package com.ludocode.ludocodebackend.progress.app.port.out

import java.util.UUID

interface CatalogPortForProgress {
    fun findFirstLessonIdInCourse(courseId: UUID): UUID?
    fun findModuleIdForLesson(lessonId: UUID): UUID?
}