package com.ludocode.ludocodebackend.progress.api.dto.request

import com.ludocode.ludocodebackend.exercise.ExerciseAnswer

data class ExerciseAttemptRequest(
    val answer: ExerciseAnswer
)