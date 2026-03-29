package com.ludocode.ludocodebackend.support.snapshot

import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import java.util.*

data class CourseSnap(
    val courseId: UUID,
    val title: String = "",
    val courseType: CourseType,
    val courseIcon: String,
    val language: String?,
    val modules: List<ModuleSnap>
)
