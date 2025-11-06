package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import java.util.UUID

data class LessonSnap(
    val id: UUID,
    var title: String,
    val orderIndex: Int,
    val exercises: List<ExerciseSnap>
)
