package com.ludocode.ludocodebackend.lesson.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.lesson.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.lesson.app.service.LessonService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(
    name = "Lesson",
    description = "Operations related to fetching static course content"
)
@RestController
@RequestMapping(ApiPaths.LESSONS.BASE)
class LessonController(private val lessonService: LessonService) {


    @Operation(
        summary = "Get exercises for lesson",
        description = """
        Returns all exercises associated with the provided lesson id.
        Includes lesson metadata, options, and ordering. 
        """
    )
    @GetMapping(ApiPaths.LESSONS.EXERCISES)
    fun getExercisesByLessonId(@PathVariable lessonId: UUID): ResponseEntity<List<ExerciseResponse>> {
        return withMdc(LogFields.LESSON_ID to lessonId.toString()) {
            ResponseEntity.ok(lessonService.getExercisesByLessonId(lessonId))
        }
    }

    @Operation(
        summary = "Get lessons by ID list",
        description = "Returns lesson metadata for the provided list of lesson IDs."
    )
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    fun getLessonsByIdList(
        @RequestParam lessonIds: List<UUID>,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<List<LessonResponse>> {
        return ResponseEntity.ok(lessonService.getLessonsByIds(lessonIds, userId))
    }


}