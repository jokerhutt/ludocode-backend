package com.ludocode.ludocodebackend.progress.infra.projection

import java.util.UUID

interface CourseProgressWithModuleProjection {
    fun getCourseId(): UUID
    fun getUserId(): UUID
    fun getCurrentLessonId(): UUID
    fun getModuleId(): UUID?
}