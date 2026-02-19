package com.ludocode.ludocodebackend.lesson.api.dto.response

import java.util.*

data class LessonResponse(
    val id: UUID,
    val title: String,
    val orderIndex: Int,
    var isCompleted: Boolean
)