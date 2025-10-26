package com.ludocode.ludocodebackend.catalog.api.dto.admin.response

import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson

data class CCLessonResponse(
    val lesson: Lesson,
    val exercises: List<ExerciseResponse>
)
