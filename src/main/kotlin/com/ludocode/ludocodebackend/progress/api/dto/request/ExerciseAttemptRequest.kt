package com.ludocode.ludocodebackend.progress.api.dto.request

import java.util.UUID

data class ExerciseAttemptRequest(
    val exerciseId: UUID,
    val isCorrect: Boolean,
    val answer: List<String>,
)
