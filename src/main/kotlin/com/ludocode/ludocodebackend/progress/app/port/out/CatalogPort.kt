package com.ludocode.ludocodebackend.progress.app.port.out

import java.util.UUID

interface CatalogPort {
    fun findFirstLessonIdInCourse(courseId: UUID): UUID?
}