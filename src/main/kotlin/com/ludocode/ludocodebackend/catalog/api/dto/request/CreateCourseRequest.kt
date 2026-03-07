package com.ludocode.ludocodebackend.catalog.api.dto.request

import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import java.util.*

data class CreateCourseRequest(
    val courseTitle: String,
    val requestHash: UUID,
    val description: String?,
    val courseType: CourseType,
    val courseIcon: String,
    val languageId: Long?
)