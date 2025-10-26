package com.ludocode.ludocodebackend.catalog.api.dto.admin.snapshot

import java.util.UUID

data class LessonSnap(
    val id: UUID?,                         // null = new
    val tempId: UUID,                      // client-side id for new rows
    val title: String,
    val exercises: List<ExerciseSnap>
)
