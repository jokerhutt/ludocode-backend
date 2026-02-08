package com.ludocode.ludocodebackend.catalog.api.dto.response

import com.ludocode.ludocodebackend.catalog.domain.entity.Subject
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import java.util.UUID

data class CourseResponse(
    val id: UUID,
    val title: String,
    val courseType: CourseType,
    val subject: CourseSubjectResponse
)

data class CourseSubjectResponse(
    val subjectId: Long,
    val slug: String,
    val name: String,
    val codeLanguage: String?
)
