package com.ludocode.ludocodebackend.progress.api.controller.internal

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(InternalPathConstants.ICOURSEPROGRESS)
class InternalCourseProgressController(private val courseProgressUseCase: CourseProgressUseCase) {

    @PostMapping(InternalPathConstants.ICOURSEPROGRESSFINDCREATE)
    fun createCourseProgress(@PathVariable courseId: UUID, @PathVariable userId: UUID ) : ResponseEntity<CourseProgressResponseWithEnrolled> {
        return ResponseEntity.ok(courseProgressUseCase.findOrCreate(userId, courseId))
    }


}