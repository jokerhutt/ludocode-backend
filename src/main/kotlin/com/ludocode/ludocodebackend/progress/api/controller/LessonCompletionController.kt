package com.ludocode.ludocodebackend.progress.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.app.service.LessonCompletionService
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(
    name = "Lesson Completions",
    description = "Operations related to submissions for lessons"
)
@SecurityRequirement(name = "sessionAuth")
@RestController
@RequestMapping(ApiPaths.PROGRESS.COMPLETION.BASE)
class LessonCompletionController(private val lessonCompletionService: LessonCompletionService) {

    @Operation(
        summary = "Submit lesson completion",
        description = """
        Submits the completion result for a lesson for the currently authenticated user.
        Evaluates the submitted lesson attempts, updates lesson and course progress,
        and applies rewards such as coins and streak updates.
        Returns the lesson completion result and updated progress state.
        Requires a valid session cookie to be present. 
        """
        )
    @PostMapping
    fun submitLessonCompletion(@RequestBody request: LessonSubmissionRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<LessonCompletionPacket> {
        return ResponseEntity.ok(lessonCompletionService.submitLessonCompletion(request, userId))
    }

}