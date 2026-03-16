package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import java.util.*

data class CurriculumDraftSnapshot(
    var modules: List<ModuleDraftSnapshot>
)

data class ModuleDraftSnapshot(
    val id: UUID,
    val title: String,
    var lessons: List<LessonDraftSnapshot>,
)

data class LessonDraftSnapshot(
    val id: UUID,
    val lessonType: LessonType,
    var title: String,
)

data class LessonCurriculumDraftSnapshot(
    var exercises: List<ExerciseSnap>,
    var projectSnapshot: ProjectSnapshot? = null,
    var lessonType: LessonType,
)