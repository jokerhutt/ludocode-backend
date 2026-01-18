package com.ludocode.ludocodebackend.progress.api.dto.request

import java.util.UUID

data class LessonSubmissionRequest(
    val submissionId: UUID,
    val lessonId: UUID,
    val submissions: List<ExerciseSubmissionRequest>
)
