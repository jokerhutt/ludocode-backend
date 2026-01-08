package com.ludocode.ludocodebackend.progress.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.app.service.CourseProgressService
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.PROGRESS.COURSES.BASE)
class CourseProgressController(private val courseProgressService: CourseProgressService) {

    @GetMapping
    fun getProgressByCourseIds(@RequestParam courseIds: List<UUID>, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<CourseProgressResponse>> {
        return ResponseEntity.ok(courseProgressService.findCourseProgressList(courseIds, userId))
    }

    @GetMapping(ApiPaths.PROGRESS.COURSES.CURRENT)
    fun getCurrentCourseId(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UUID?> {
        return ResponseEntity.ok(courseProgressService.findCurrentCourseId(userId))
    }

    @GetMapping(ApiPaths.PROGRESS.COURSES.ENROLLED)
    fun getEnrolledCourseIds(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<UUID>> {
        return ResponseEntity.ok(courseProgressService.getEnrolledCourseIds(userId))
    }

    @PostMapping(ApiPaths.PROGRESS.COURSES.RESET)
    fun resetUserCourseProgress (@PathVariable courseId: UUID, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<CourseProgressResponse> {
        return ResponseEntity.ok(courseProgressService.resetUserCourseProgress(userId, courseId))
    }

    @PutMapping(ApiPaths.PROGRESS.COURSES.CURRENT)
    fun updateCurrentCourse(
        @RequestBody request: ChangeCourseRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<CourseProgressResponseWithEnrolled> {
        return ResponseEntity.ok(courseProgressService.findOrCreate(userId, request.newCourseId))
    }

}