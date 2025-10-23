package com.ludocode.ludocodebackend.progress.api.controller.external

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.PROGRESS_COMPLETION)
class LessonCompletionController {

    @PostMapping(PathConstants.SUBMIT_COMPLETION)
    fun submitLessonCompletion(@RequestBody request: LessonSubmissionRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<LessonCompletionResponse> {

    }



}