package com.ludocode.ludocodebackend.catalog.api.dto.admin.request

import java.util.UUID

data class LessonDiffRequest(
    val lessonId: UUID?,
    val tempId: UUID,
    val title: String,
    val changedExercises: List<ExerciseDiffRequest>,
    val exercisesToDelete: List<UUID>
)
