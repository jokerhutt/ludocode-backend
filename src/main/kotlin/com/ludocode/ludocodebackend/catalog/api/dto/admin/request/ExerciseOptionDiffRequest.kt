package com.ludocode.ludocodebackend.catalog.api.dto.admin.request

import java.util.UUID

data class ExerciseOptionDiffRequest (
    val id: UUID,
    val content: String,
    val answerOrder: Int?,
)