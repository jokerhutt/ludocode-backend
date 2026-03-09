package com.ludocode.ludocodebackend.feedback.api.dto.request

import com.ludocode.ludocodebackend.feedback.domain.enums.FeedbackType
import java.util.UUID

data class FeedbackSubmissionRequest(
    val content: String,
    val feedbackType: FeedbackType,
    val entityId: UUID?
)
