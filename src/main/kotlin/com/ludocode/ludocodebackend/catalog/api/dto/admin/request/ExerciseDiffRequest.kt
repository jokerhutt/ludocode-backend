package com.ludocode.ludocodebackend.catalog.api.dto.admin.request

import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import java.util.UUID

data class ExerciseDiffRequest (
    val id: UUID?,
    val title: String?,
    val prompt: String,
    val exerciseType: ExerciseType,
    val currentVersion: Int,
    val options: List<ExerciseOptionDiffRequest>
)