package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseTreeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.app.service.CatalogService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping(PathConstants.CATALOG)
class CatalogController(private val catalogService: CatalogService) {

    @GetMapping(PathConstants.COURSES_ALL)
    fun getAllCourses(): ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(catalogService.getAllCourses())
    }

    @GetMapping(PathConstants.COURSE_TREE)
    fun getCourseTree(@PathVariable courseId: UUID, @PathVariable userId: UUID): ResponseEntity<CourseTreeResponse> {
        return ResponseEntity.ok(catalogService.getCourseTree(userId, courseId))
    }

    @GetMapping(PathConstants.EXERCISES_LESSON_ID)
    fun getExercisesByLessonId(@PathVariable lessonId: UUID) : ResponseEntity<List<ExerciseResponse>> {
        return ResponseEntity.ok(catalogService.getExercisesByLessonId(lessonId));
    }

    @GetMapping(PathConstants.MODULES_COURSE_ID)
    fun getModulesByCourseId(@PathVariable courseId: UUID) : ResponseEntity<List<ModuleResponse>> {
        return ResponseEntity.ok(catalogService.getModulesByCourseId(courseId))
    }

    @GetMapping(PathConstants.LESSONS_MODULE_ID)
    fun getLessonsByModuleId(@PathVariable userId: UUID, @PathVariable moduleId: UUID) : ResponseEntity<List<LessonResponse>> {
        return ResponseEntity.ok(catalogService.getLessonsByModuleId(moduleId, userId))
    }








}