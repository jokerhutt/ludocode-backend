package com.ludocode.ludocodebackend.feedback.app.service

import com.ludocode.ludocodebackend.feedback.api.dto.request.FeedbackSubmissionRequest
import com.ludocode.ludocodebackend.feedback.domain.entity.Feedback
import com.ludocode.ludocodebackend.feedback.infra.repository.FeedbackRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class FeedbackService(private val feedbackRepository: FeedbackRepository, private val clock: Clock) {

    @Transactional
    fun submitFeedback(req: FeedbackSubmissionRequest) {

        feedbackRepository.save(Feedback(
            id = UUID.randomUUID(),
            content = req.content,
            feedbackType = req.feedbackType,
            entityId = req.entityId,
            createdAt = OffsetDateTime.now(clock)
        ))

    }

}



