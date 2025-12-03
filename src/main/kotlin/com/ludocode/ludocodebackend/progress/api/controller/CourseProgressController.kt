package com.ludocode.ludocodebackend.progress.api.controller

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.app.service.CourseProgressService
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.PROGRESS_COURSE)
class CourseProgressController(private val courseProgressService: CourseProgressService) {

    @GetMapping(PathConstants.COURSE_PROGRESS_FROM_COURSE_IDS)
    fun getProgressByCourseIds(@RequestParam courseIds: List<UUID>, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<CourseProgressResponse>> {
        return ResponseEntity.ok(courseProgressService.findCourseProgressList(courseIds, userId))
    }

    @GetMapping(PathConstants.CURRENT_COURSE)
    fun getCurrentCourseId(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UUID?> {
        return ResponseEntity.ok(courseProgressService.findCurrentCourseId(userId))
    }

    @GetMapping(PathConstants.ENROLLED_IDS)
    fun getEnrolledCourseIds(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<UUID>> {
        return ResponseEntity.ok(courseProgressService.getEnrolledCourseIds(userId))
    }

    @PostMapping(PathConstants.RESET_PROGRESS)
    fun resetUserCourseProgress (@PathVariable courseId: UUID, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<CourseProgressResponse> {
        return ResponseEntity.ok(courseProgressService.resetUserCourseProgress(userId, courseId))
    }

    @PostMapping(PathConstants.UPDATE_COURSE)
    fun updateCurrentCourse(
        @RequestBody request: ChangeCourseRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<CourseProgressResponseWithEnrolled> {
        return ResponseEntity.ok(courseProgressService.findOrCreate(userId, request.newCourseId))
    }

}