package com.ludocode.ludocodebackend.catalog.api.dto.yaml

import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import java.util.UUID

data class CurriculumYamlRoot (
    val title: String,
    val description: String? = "",
    val courseType: CourseType,
    val courseIcon: String,
    val languageId: Long?,
    val modules: List<CurriculumYamlModule>
)

data class CurriculumYamlModule (
    val id: UUID?,
    val title: String,
    val lessons: List<CurriculumYamlLesson>
)

data class CurriculumYamlLesson (
    val id: UUID?,
    val title: String,
    val exercises: List<ExerciseSnap>
)