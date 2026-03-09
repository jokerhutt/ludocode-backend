package com.ludocode.ludocodebackend.feedback.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.feedback.api.dto.request.FeedbackSubmissionRequest
import com.ludocode.ludocodebackend.feedback.domain.enums.FeedbackType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import java.util.UUID
import kotlin.test.Test

class FeedbackIT : AbstractIntegrationTest() {

    @Test
    fun submitFeedback_returnsOk() {

        val req = FeedbackSubmissionRequest(
            content = "Wow awesome app",
            feedbackType = FeedbackType.GENERAL,
            entityId = null,
        )

        submitPostFeedback(req)
    }

    @Test
    fun submitsFeedbackForExercise_returnsOk() {
        val req = FeedbackSubmissionRequest(
            content = "Wow this exercise sucks",
            feedbackType = FeedbackType.EXERCISE,
            entityId = UUID.randomUUID()
        )

        submitPostFeedback(req)

    }


    private fun submitPostFeedback(req: FeedbackSubmissionRequest) =
        TestRestClient.postNoContent(ApiPaths.FEEDBACK.BASE, user1.id, req)



}