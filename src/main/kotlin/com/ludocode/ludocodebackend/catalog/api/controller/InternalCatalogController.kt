package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogUseCase
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
    private val catalogUseCase: CatalogUseCase
) {

    @GetMapping(InternalPathConstants.IFIRST_LESSON_ID)
    fun getFirstLessonIdInCourse(@PathVariable courseId: UUID) : ResponseEntity<UUID> {
        return ResponseEntity.ok(catalogUseCase.findFirstLessonIdInCourse(courseId))
    }

    @GetMapping(InternalPathConstants.ILESSON_MODULE_ID)
    fun getModuleIdForLesson(@PathVariable lessonId: UUID) : ResponseEntity<UUID> {
        return ResponseEntity.ok(catalogUseCase.findModuleIdForLesson(lessonId))
    }

}