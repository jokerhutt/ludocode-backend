package com.ludocode.ludocodebackend.progress.api.dto.request

import java.util.*

data class ExerciseSubmissionRequest(
    val exerciseId: UUID,
    val version: Int,
    val attempts: List<ExerciseAttemptRequest>
)
