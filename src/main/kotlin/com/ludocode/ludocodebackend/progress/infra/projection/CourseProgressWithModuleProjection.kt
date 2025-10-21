package com.ludocode.ludocodebackend.progress.infra.projection

import java.util.UUID

interface CourseProgressWithModuleProjection {
    fun courseId(): UUID
    fun userId(): UUID
    fun currentLessonId(): UUID
    fun moduleId(): UUID?
}