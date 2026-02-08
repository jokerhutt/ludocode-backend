package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseSubjectResponse
import com.ludocode.ludocodebackend.catalog.app.service.SubjectService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Subjects",
    description = "Operations related to course subjects available on the platform"
)
@RestController
@RequestMapping(ApiPaths.SUBJECTS.BASE)
class SubjectController(
    private val subjectService: SubjectService
) {

    @Operation(
        summary = "Get all available subjects",
        description = """
        Returns a list of all course subjects available on the platform.
        The returned data can be used to populate subject selectors and to
        organize courses and learning paths by topic.
        Requires an authenticated user session.
        """
    )
    @GetMapping
    fun getSubjects(): ResponseEntity<List<CourseSubjectResponse>> {
        return ResponseEntity.ok(subjectService.getAllSubjects())
    }

}