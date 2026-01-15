package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("admin", "test")
@RestController
@RequestMapping(ApiPaths.SNAPSHOTS.BASE)
class CatalogAdminController(
                             private val snapshotService: SnapshotService
) {

    @Operation(summary = "Submit course snapshot for the selected course id",
        description = """
        Applies a course snapshot to the specified course.
        This operation replaces the existing course structure with the provided snapshot, including modules, lessons, exercises, and ordering.
        Intended for catalog modification & admin use.
        Returns the persisted course snapshot after the update.  
        """
        )
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping(ApiPaths.SNAPSHOTS.BY_COURSE)
    fun applyCourseSnapshot(@RequestBody s: CourseSnap, @PathVariable courseId: UUID, @AuthenticationPrincipal (expression = "userId") userId: UUID) : ResponseEntity<CourseSnap> {
        return ResponseEntity.ok(snapshotService.applyNewSnapshot(s))
    }

    @Operation(summary = "Get course snapshot for the selected course id",
        description = """
        Returns a snapshot of the course structure for the specified course.
        Includes modules, lessons, exercises, and exercise options in a nested object.
        Intended for catalog modification & admin use. 
        """
        )
    @GetMapping(ApiPaths.SNAPSHOTS.BY_COURSE)
    fun getSnapshotsByCourseId(@PathVariable courseId: UUID) : ResponseEntity<CourseSnap> {
        return ResponseEntity.ok(snapshotService.getCourseSnapshot(courseId))
    }

    @Operation(summary = "Create course",
        description = """
        Creates a new course with the provided title.
        Initializes the course with a default module, lesson, and exercise.
        Returns metadata for the newly created course. 
        """
        )
    @PostMapping(ApiPaths.SNAPSHOTS.COURSE)
    fun createCourse(@RequestBody request: CreateCourseRequest) : ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(snapshotService.createCourse(request))
    }


}