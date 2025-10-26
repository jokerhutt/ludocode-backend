package com.ludocode.ludocodebackend.progress.api.dto.request

import java.util.UUID

data class ExerciseSubmissionRequest(
    val exerciseId: UUID,
    val attempts: List<ExerciseAttemptRequest>,
    val version: Int
)
