package com.ludocode.ludocodebackend.catalog.api.dto.response

import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import java.util.UUID

data class ExerciseResponse (

    val id: UUID,
    val title: String,
    val subtitle: String?,
    val prompt: String?,
    val exerciseType: ExerciseType,
    val lessonId: UUID,
    val correctOptions: List<ExerciseOptionResponse>,
    val distractors: List<ExerciseOptionResponse>,
    val version: Int,
    val orderIndex: Int

)