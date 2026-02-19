package com.ludocode.ludocodebackend.catalog.infra.projection

import java.util.*

interface FlatModuleLessonRow {
    fun getModuleId(): UUID
    fun getModuleOrder(): Int
    fun getLessonId(): UUID?
    fun getLessonOrder(): Int?
}