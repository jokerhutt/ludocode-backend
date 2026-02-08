package com.ludocode.ludocodebackend.catalog.api.dto.snapshot
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import java.util.UUID

data class CourseSnap(
    val courseId: UUID,
    val title: String = "",
    val courseType: CourseType,
    val courseSubject: SubjectSnap,
    val language: LanguageMetadata?,
    val modules: List<ModuleSnap>
)

data class SubjectSnap(
    val slug: String,
    val name: String,
)
