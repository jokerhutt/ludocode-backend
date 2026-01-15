package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.catalog.app.service.CatalogService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.CATALOG.BASE)
class CatalogController(private val catalogService: CatalogService) {

    @Operation(summary = "Get all available courses",
        description = """
        Returns a list of all available courses in the catalog.
        Includes basic course metadata such as ID and title. 
        """
        )
    @GetMapping(ApiPaths.CATALOG.COURSES)
    fun getAllCourses(): ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(catalogService.getAllCourses())
    }

    @Operation(summary = "Get course tree",
        description = """
        Returns the full course structure for the specified course.
        Includes modules and lessons in a flattened tree format suitable for rendering. 
        """
        )
    @GetMapping(ApiPaths.CATALOG.COURSE_TREE)
    fun getCourseTree(@PathVariable courseId: UUID): ResponseEntity<FlatCourseTreeResponse> {
        return ResponseEntity.ok(catalogService.getFlatCourseTree(courseId))
    }

    @Operation(summary = "Get exercises for lesson",
        description = """
        Returns all exercises associated with the provided lesson id.
        Includes exercise metadata, options, and ordering. 
        """
        )
    @GetMapping(ApiPaths.CATALOG.LESSON_EXERCISES)
    fun getExercisesByLessonId(@PathVariable lessonId: UUID) : ResponseEntity<List<ExerciseResponse>> {
        return ResponseEntity.ok(catalogService.getExercisesByLessonId(lessonId));
    }

    @Operation(summary = "Get modules by ID list", description = "Returns module metadata for the provided list of module IDs.")
    @GetMapping(ApiPaths.CATALOG.MODULES)
    fun getModulesByIdList(@RequestParam moduleIds: List<UUID>) : ResponseEntity<List<ModuleResponse>> {
        return ResponseEntity.ok(catalogService.getModulesByIds(moduleIds))
    }

    @Operation(summary = "Get lessons by ID list", description = "Returns lesson metadata for the provided list of lesson IDs.")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping(ApiPaths.CATALOG.LESSONS)
    fun getLessonsByIdList(@RequestParam lessonIds: List<UUID>, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<LessonResponse>> {
        return ResponseEntity.ok(catalogService.getLessonsByIds(lessonIds, userId))
    }

}