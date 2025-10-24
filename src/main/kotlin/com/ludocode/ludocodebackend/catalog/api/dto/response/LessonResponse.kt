package com.ludocode.ludocodebackend.catalog.api.dto.response

import java.util.UUID

data class LessonResponse(
    val id: UUID,
    val title: String,
    val orderIndex: Int,
    var isCompleted: Boolean
)
