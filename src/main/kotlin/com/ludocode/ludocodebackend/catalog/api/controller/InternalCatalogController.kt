package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogUseCase
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotBuilderService
import com.ludocode.ludocodebackend.catalog.infra.projection.LessonIdTreeProjection
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(InternalPathConstants.ICATALOG)
class InternalCatalogController (
    private val catalogUseCase: CatalogUseCase,
    private val snapshotBuilderService: SnapshotBuilderService
) {

    @GetMapping(InternalPathConstants.ILESSON_ID_TREE)
    fun getFullLesson(@PathVariable lessonId: UUID) : ResponseEntity<LessonTreeWithIdDTO> {
        return ResponseEntity.ok(catalogUseCase.findLessonIdTree(lessonId))
    }

    @GetMapping(InternalPathConstants.IEXERCISE_SNAPSHOT)
    fun getExerciseSnapshotById(@PathVariable exerciseId: UUID) : ResponseEntity<ExerciseSnap> {
        return ResponseEntity.ok(catalogUseCase.findExerciseSnapshotById(exerciseId))
    }

    @GetMapping(InternalPathConstants.ILESSON_BY_ID)
    fun getLessonResponseById(@PathVariable lessonId: UUID, @PathVariable userId: UUID) : ResponseEntity<LessonResponse> {
        return ResponseEntity.ok(catalogUseCase.findLessonResponseById(lessonId, userId))
    }

    @GetMapping(InternalPathConstants.IFIRST_LESSON_ID)
    fun getFirstLessonIdInCourse(@PathVariable courseId: UUID) : ResponseEntity<UUID> {
        return ResponseEntity.ok(catalogUseCase.findFirstLessonIdInCourse(courseId))
    }

    @GetMapping(InternalPathConstants.ILESSON_MODULE_ID)
    fun getModuleIdForLesson(@PathVariable lessonId: UUID) : ResponseEntity<UUID> {
        return ResponseEntity.ok(catalogUseCase.findModuleIdForLesson(lessonId))
    }

    @GetMapping(InternalPathConstants.ILESSON_COURSE_ID)
    fun getCourseIdForLesson(@PathVariable lessonId: UUID) : ResponseEntity<UUID> {
        return ResponseEntity.ok(catalogUseCase.findCourseIdForLesson(lessonId))
    }

    @GetMapping(InternalPathConstants.INEXT_LESSON_ID)
    fun getNextLessonId(@PathVariable lessonId: UUID) : ResponseEntity<UUID> {
        return ResponseEntity.ok(catalogUseCase.findNextLessonId(lessonId))
    }

}