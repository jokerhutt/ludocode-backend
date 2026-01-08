package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.catalog.app.service.CatalogService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.constants.PathConstants
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

    @GetMapping(ApiPaths.CATALOG.COURSES)
    fun getAllCourses(): ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(catalogService.getAllCourses())
    }

    @GetMapping(ApiPaths.CATALOG.COURSE_TREE)
    fun getCourseTree(@PathVariable courseId: UUID): ResponseEntity<FlatCourseTreeResponse> {
        return ResponseEntity.ok(catalogService.getFlatCourseTree(courseId))
    }

    @GetMapping(ApiPaths.CATALOG.LESSON_EXERCISES)
    fun getExercisesByLessonId(@PathVariable lessonId: UUID) : ResponseEntity<List<ExerciseResponse>> {
        return ResponseEntity.ok(catalogService.getExercisesByLessonId(lessonId));
    }

    @GetMapping(ApiPaths.CATALOG.MODULES)
    fun getModulesByIdList(@RequestParam moduleIds: List<UUID>) : ResponseEntity<List<ModuleResponse>> {
        return ResponseEntity.ok(catalogService.getModulesByIds(moduleIds))
    }

    @GetMapping(ApiPaths.CATALOG.LESSONS)
    fun getLessonsByIdList(@RequestParam lessonIds: List<UUID>, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<LessonResponse>> {
        return ResponseEntity.ok(catalogService.getLessonsByIds(lessonIds, userId))
    }

}