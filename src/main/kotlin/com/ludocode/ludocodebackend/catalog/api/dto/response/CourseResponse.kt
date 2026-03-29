package com.ludocode.ludocodebackend.catalog.api.dto.response
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseStatus
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.tag.api.dto.TagMetadata
import java.util.*

data class CourseResponse(
    val id: UUID,
    val title: String,
    val courseType: CourseType,
    val courseIcon: String,
    val codeLanguage: String?,
    val tags: List<TagMetadata>,
    val courseStatus: CourseStatus,
    val description: String
)