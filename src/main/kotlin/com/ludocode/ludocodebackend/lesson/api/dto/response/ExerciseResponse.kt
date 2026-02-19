package com.ludocode.ludocodebackend.lesson.api.dto.response

import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import java.util.*

data class ExerciseResponse(

    val id: UUID,
    val title: String,
    val subtitle: String?,
    val prompt: String?,
    val exerciseType: ExerciseType,
    val exerciseMedia: String?,
    val lessonId: UUID,
    val correctOptions: List<ExerciseOptionResponse>,
    val distractors: List<ExerciseOptionResponse>,
    val version: Int,
    val orderIndex: Int

)