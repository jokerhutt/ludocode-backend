package com.ludocode.ludocodebackend.tag.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.SubjectMetadata
import com.ludocode.ludocodebackend.tag.app.service.TagService
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
class TagController(
    private val tagService: TagService
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
    fun getSubjects(): ResponseEntity<List<SubjectMetadata>> {
        return ResponseEntity.ok(tagService.getAllSubjects())
    }

}