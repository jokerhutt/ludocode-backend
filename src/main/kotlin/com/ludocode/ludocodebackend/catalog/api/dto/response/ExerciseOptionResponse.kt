package com.ludocode.ludocodebackend.catalog.api.dto.response

import java.util.UUID

data class ExerciseOptionResponse(
    val id: UUID,
    val content: String,
    val answerOrder: Int?
)
