package com.ludocode.ludocodebackend.feedback.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.feedback.api.dto.request.FeedbackSubmissionRequest
import com.ludocode.ludocodebackend.feedback.app.service.FeedbackService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.FEEDBACK.BASE)
class FeedbackController(private val feedbackService: FeedbackService) {

    @PostMapping
    fun submitFeedback (@RequestBody req: FeedbackSubmissionRequest) : ResponseEntity<Void> {
        feedbackService.submitFeedback(req)
        return ResponseEntity.noContent().build()
    }

}