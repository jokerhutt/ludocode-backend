package com.ludocode.ludocodebackend.catalog.api.controller.admin

import com.ludocode.ludocodebackend.catalog.api.dto.request.ChangeLanguageRequest
import com.ludocode.ludocodebackend.catalog.api.dto.request.ChangeSubjectRequest
import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlRoot
import com.ludocode.ludocodebackend.catalog.app.service.CatalogService
import com.ludocode.ludocodebackend.catalog.app.service.admin.CurriculumSnapshotService
import com.ludocode.ludocodebackend.catalog.app.service.admin.CurriculumYamlService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(
    name = "Admin Catalog",
    description = "Admin Operations related to creating & modifying course structure"
)
@Profile("admin", "dev", "test", "devtestadmin")
@RestController
@RequestMapping(ApiPaths.SNAPSHOTS.ADMIN_BASE)
class CatalogAdminController(
    private val curriculumSnapshotService: CurriculumSnapshotService,
    private val catalogService: CatalogService,
    private val curriculumYamlService: CurriculumYamlService
) {

    @GetMapping(ApiPaths.SNAPSHOTS.BY_COURSE_CURRICULUM, params = ["mode!=yaml"])
    fun getCourseCurriculumByCourseId(@PathVariable courseId: UUID): ResponseEntity<CurriculumDraftSnapshot> {
        return ResponseEntity.ok(curriculumSnapshotService.buildCurriculumSnapshot(courseId))
    }

    @GetMapping(
        ApiPaths.SNAPSHOTS.BY_COURSE_CURRICULUM,
        params = ["mode=yaml"],
        produces = ["application/x-yaml"]
    )
    fun getYamlCourseCurriculumByCourseId(
        @PathVariable courseId: UUID
    ): ResponseEntity<String> {

        val yaml = curriculumYamlService.exportYaml(courseId)

        return ResponseEntity
            .ok()
            .contentType(MediaType("application", "x-yaml"))
            .body(yaml)
    }

    @PutMapping(ApiPaths.SNAPSHOTS.COURSE_SUBJECT)
    fun changeSubject(
        @RequestBody req: ChangeSubjectRequest,
        @PathVariable courseId: UUID
    ): ResponseEntity<List<CourseResponse>> {
        val subjectId = req.subjectId
        catalogService.updateCourseSubject(courseId, subjectId)
        return ResponseEntity.ok(catalogService.getAllCourses())
    }

    @PutMapping(ApiPaths.SNAPSHOTS.COURSE_LANGUAGE)
    fun changeLanguage(
        @RequestBody req: ChangeLanguageRequest,
        @PathVariable courseId: UUID
    ): ResponseEntity<List<CourseResponse>> {
        val languageId = req.languageId
        catalogService.updateCourseLanguage(courseId, languageId)
        return ResponseEntity.ok(catalogService.getAllCourses())
    }

    @PutMapping(ApiPaths.SNAPSHOTS.BY_COURSE_CURRICULUM, params = ["mode!=yaml"])
    fun applyCurriculumSnapshot(
        @RequestBody snapshot: CurriculumDraftSnapshot,
        @PathVariable courseId: UUID
    ): ResponseEntity<CurriculumDraftSnapshot> {
        return ResponseEntity.ok(
            curriculumSnapshotService.applyCurriculumDiffs(courseId, snapshot)
        )
    }

    @PutMapping(
        ApiPaths.SNAPSHOTS.BY_COURSE_CURRICULUM,
        params = ["mode=yaml"],
        consumes = ["application/x-yaml", "text/yaml", "application/yaml"])
    fun applyCurriculumYaml(
        @RequestBody yaml: CurriculumYamlRoot,
        @PathVariable courseId: UUID
    ): ResponseEntity<Void> {
        curriculumYamlService.importYaml(courseId, yaml)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Create course",
        description = """
        Creates a new course from a given yaml file.
        """
    )
    @PostMapping(ApiPaths.SNAPSHOTS.COURSE,
        params = ["mode=yaml"],
        consumes = ["application/x-yaml", "text/yaml", "application/yaml"])
    fun createCourseYaml(@RequestBody request: CurriculumYamlRoot): ResponseEntity<Void> {
        curriculumYamlService.importYaml( root = request)
        return ResponseEntity.noContent().build()
    }


    @Operation(
        summary = "Create course",
        description = """
        Creates a new course with the provided title.
        Initializes the course with a default module, lesson, and lesson.
        Returns metadata for the newly created course. 
        """
    )
    @PostMapping(ApiPaths.SNAPSHOTS.COURSE)
    fun createCourse(@RequestBody request: CreateCourseRequest): ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(curriculumSnapshotService.createCourseReturningList(request))
    }


}