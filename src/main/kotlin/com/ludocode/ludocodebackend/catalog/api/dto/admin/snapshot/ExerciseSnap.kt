package com.ludocode.ludocodebackend.catalog.api.dto.admin.snapshot

import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import java.util.UUID

data class ExerciseSnap(
    val id: UUID?,                         // null = new exercise (v1)
    val title: String,
    val prompt: String,
    val exerciseType: ExerciseType,
    val options: List<OptionSnap>          // no IDs; fresh per version
)