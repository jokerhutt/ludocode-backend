package com.ludocode.ludocodebackend.progress.api.dto.request

import java.util.*

data class LessonSubmissionRequest(
    val submissionId: UUID,
    val lessonId: UUID,
    val courseId: UUID,
    val submissions: List<ExerciseSubmissionRequest>
)
