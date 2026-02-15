package com.ludocode.ludocodebackend.catalog.api.controller.admin

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.app.service.admin.CurriculumSnapshotService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(
    name = "Admin Catalog",
    description = "Admin Operations related to creating & modifying course structure"
)
@Profile("admin", "devadmin", "test")
@RestController
@RequestMapping(ApiPaths.SNAPSHOTS.ADMIN_BASE)
class CatalogAdminController(
    private val curriculumSnapshotService: CurriculumSnapshotService
) {


    @Operation(summary = "Get course snapshot for the selected course id",
        description = """
        Returns a snapshot of the course structure for the specified course.
        Includes modules, lessons, exercises, and lesson options in a nested object.
        Intended for catalog modification & admin use. 
        """
        )
    @GetMapping(ApiPaths.SNAPSHOTS.BY_COURSE)
    fun getSnapshotsByCourseId(@PathVariable courseId: UUID) : ResponseEntity<CourseSnap> {
        return ResponseEntity.ok(curriculumSnapshotService.buildCourseSnapshot(courseId))
    }

    @GetMapping(ApiPaths.SNAPSHOTS.BY_COURSE_CURRICULUM)
    fun getCourseCurriculumByCourseId(@PathVariable courseId: UUID) : ResponseEntity<CurriculumDraftSnapshot> {
        return ResponseEntity.ok(curriculumSnapshotService.buildCurriculumSnapshot(courseId))
    }

    @PutMapping(ApiPaths.SNAPSHOTS.BY_COURSE_CURRICULUM)
    fun applyCurriculumSnapshot(@RequestBody snapshot: CurriculumDraftSnapshot, @PathVariable courseId: UUID): ResponseEntity<CurriculumDraftSnapshot> {
        return ResponseEntity.ok(curriculumSnapshotService.applyCurriculumDiffs(courseId, snapshot))
    }


    @Operation(summary = "Create course",
        description = """
        Creates a new course with the provided title.
        Initializes the course with a default module, lesson, and lesson.
        Returns metadata for the newly created course. 
        """
        )
    @PostMapping(ApiPaths.SNAPSHOTS.COURSE)
    fun createCourse(@RequestBody request: CreateCourseRequest) : ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(curriculumSnapshotService.createCourse(request))
    }


}