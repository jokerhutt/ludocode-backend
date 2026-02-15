package com.ludocode.ludocodebackend.lesson.api.dto.response

import java.util.UUID

data class ExerciseOptionResponse(
    val id: UUID,
    val content: String,
    val answerOrder: Int?,
    val exerciseVersion: Int
)