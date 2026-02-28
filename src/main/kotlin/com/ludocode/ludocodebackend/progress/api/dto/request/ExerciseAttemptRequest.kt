package com.ludocode.ludocodebackend.progress.api.dto.request

import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExerciseAnswer

data class ExerciseAttemptRequest(
    val answer: ExerciseAnswer
)