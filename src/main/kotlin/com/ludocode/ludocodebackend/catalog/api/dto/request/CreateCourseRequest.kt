package com.ludocode.ludocodebackend.catalog.api.dto.request

import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.playground.domain.entity.CodeLanguages
import java.util.UUID

data class CreateCourseRequest(val courseTitle: String, val requestHash: UUID, val courseType: CourseType, val courseSubject: CourseSubjectRequest)

data class CourseSubjectRequest(
    val slug: String,
    val name: String,
    val codeLanguageId: Long
)