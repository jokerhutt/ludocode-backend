package com.ludocode.ludocodebackend.progress.dto.request

import java.util.UUID

data class LessonSubmissionRequest(
    val id: UUID,
    val lessonId: UUID,
    val submissions: List<ExerciseSubmissionRequest>
)
