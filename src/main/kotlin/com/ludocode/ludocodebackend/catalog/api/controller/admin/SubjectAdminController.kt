package com.ludocode.ludocodebackend.catalog.api.controller.admin

import com.ludocode.ludocodebackend.catalog.api.dto.request.SubjectRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseSubjectResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.SubjectMetadata
import com.ludocode.ludocodebackend.catalog.app.service.SubjectService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(
    name = "Admin Subjects",
    description = "Admin operations related to creating and modifying course subjects"
)
@Profile("admin", "dev", "test", "devtestadmin")
@RestController
@RequestMapping(ApiPaths.SUBJECTS.ADMIN_BASE)
class SubjectAdminController(
    private val subjectService: SubjectService
) {

    @Operation(
        summary = "Create subject",
        description = """
        Creates a new course subject.
        The subject can later be assigned to courses and used to organize the catalog.
        Intended for catalog modification & admin use.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    fun createSubject(
        @RequestBody req: SubjectMetadata
    ): ResponseEntity<List<SubjectMetadata>> {
        return ResponseEntity.ok(subjectService.createSubject(req))
    }

    @Operation(
        summary = "Update subject",
        description = """
        Updates the metadata of an existing course subject.
        Intended for catalog modification & admin use.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping(ApiPaths.SUBJECTS.BY_SUBJECT)
    fun updateSubject(
        @PathVariable subjectId: Long,
        @RequestBody req: SubjectMetadata
    ): ResponseEntity<List<SubjectMetadata>> {
        return ResponseEntity.ok(subjectService.updateSubject(subjectId, req))
    }

//    @Operation(
//        summary = "Delete subject",
//        description = """
//        Deletes the specified course subject.
//        Intended for catalog modification & admin use.
//        """
//    )
//    @SecurityRequirement(name = "sessionAuth")
//    @DeleteMapping(ApiPaths.SUBJECTS.BY_SUBJECT)
//    fun deleteSubject(
//        @PathVariable subjectId: Long
//    ): ResponseEntity<List<SubjectMetadata>> {
//        return ResponseEntity.ok(subjectService.deleteSubject(subjectId))
//    }

}