package com.ludocode.ludocodebackend.progress.infra.projection

import java.time.Instant
import java.util.*

interface CourseProgressWithModuleProjection {
    fun getCourseId(): UUID
    fun getUserId(): UUID
    fun getCurrentLessonId(): UUID
    fun getModuleId(): UUID?
    fun getUpdatedAt(): Instant
}