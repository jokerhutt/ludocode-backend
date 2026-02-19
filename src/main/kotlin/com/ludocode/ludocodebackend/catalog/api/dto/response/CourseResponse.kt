package com.ludocode.ludocodebackend.catalog.api.dto.response

import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import java.util.*

data class CourseResponse(
    val id: UUID,
    val title: String,
    val courseType: CourseType,
    val subject: CourseSubjectResponse,
    val language: LanguageMetadata?
)

data class CourseSubjectResponse(
    val subjectId: Long,
    val slug: String,
    val name: String,
)
