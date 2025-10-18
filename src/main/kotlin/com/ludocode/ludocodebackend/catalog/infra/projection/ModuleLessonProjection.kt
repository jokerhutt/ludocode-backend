package com.ludocode.ludocodebackend.catalog.infra.projection

import java.util.UUID

interface ModuleLessonProjection {
    fun getModuleId(): UUID
    fun getModuleTitle(): String
    fun getModuleOrder(): Int
    fun getCourseId(): UUID

    fun getLessonId(): UUID?
    fun getLessonTitle(): String?
    fun getLessonOrder(): Int?

    fun getIsCompleted(): Boolean
}