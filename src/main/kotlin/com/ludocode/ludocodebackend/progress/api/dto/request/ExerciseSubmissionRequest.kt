package com.ludocode.ludocodebackend.progress.api.dto.request

import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExerciseAnswer
import java.util.*

data class ExerciseSubmissionRequest(
    val exerciseId: UUID,
    val version: Int,
    val results: List<ExerciseAttemptResult>
)

data class ExerciseAttemptResult(
    val isCorrect: Boolean,
    val attempt: ExerciseAnswer
)
