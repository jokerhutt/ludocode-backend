package com.ludocode.ludocodebackend.lesson.api.dto.snapshot

import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import java.util.UUID

data class ExerciseSnap(
    val id: UUID,
    var title: String,
    var subtitle: String?,
    val prompt: String?,
    val media: String? = null,
    val exerciseType: ExerciseType,
    var correctOptions: List<OptionSnap>,
    var distractors: List<OptionSnap>
)