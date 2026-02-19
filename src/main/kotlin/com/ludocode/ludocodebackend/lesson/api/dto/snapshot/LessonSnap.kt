package com.ludocode.ludocodebackend.lesson.api.dto.snapshot

import java.util.*

data class LessonSnap(
    val id: UUID,
    var title: String,
    val orderIndex: Int,
    val exercises: List<ExerciseSnap>
)