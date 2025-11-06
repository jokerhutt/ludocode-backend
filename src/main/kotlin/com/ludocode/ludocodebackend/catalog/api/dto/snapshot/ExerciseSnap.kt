package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import java.util.UUID

data class ExerciseSnap(
    val id: UUID,
    val title: String,
    val subtitle: String?,
    val prompt: String?,
    val media: String? = null,
    val exerciseType: ExerciseType,
    val correctOptions: List<OptionSnap>,
    val distractors: List<OptionSnap>
)