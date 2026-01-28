package com.ludocode.ludocodebackend.progress.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.app.service.CourseProgressService
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressStats
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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

@SecurityRequirement(name = "sessionAuth")
@RestController
@RequestMapping(ApiPaths.PROGRESS.COURSES.BASE)
class CourseProgressController(private val courseProgressService: CourseProgressService) {

    @Operation(
        summary = "Get course progress by course IDs",
        description = """
        Returns course progress entries for the specified course IDs for the currently authenticated user.
        Requires a valid session cookie to be present
        """
    )
    @GetMapping
    fun getProgressByCourseIds(@RequestParam courseIds: List<UUID>, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<CourseProgressResponse>> {
        return ResponseEntity.ok(courseProgressService.findCourseProgressList(courseIds, userId))
    }

    @Operation(
        summary = "Get user's course stats by course IDs",
        description = """
        Returns the course stat entries (total completed lessons & total lessons) for the currently authenticated user.
        Requires a valid session cookie to be present
        """
    )
    @GetMapping(ApiPaths.PROGRESS.COURSES.STATS)
    fun getProgressStatsByCourseIds(@RequestParam courseIds: List<UUID>, @AuthenticationPrincipal(expression = "userId") userId: UUID): ResponseEntity<List<CourseProgressStats>> {
        return ResponseEntity.ok(courseProgressService.getCourseProgressStats(userId, courseIds))
    }

    @Operation(
        summary = "Get current active course",
        description = """
        Returns the course id of the currently active course for the authenticated user.
        Requires a valid session cookie to be present.
        """
    )
    @GetMapping(ApiPaths.PROGRESS.COURSES.CURRENT)
    fun getCurrentCourseId(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<UUID?> {
        return ResponseEntity.ok(courseProgressService.findCurrentCourseId(userId))
    }

    @Operation(
        summary = "Get enrolled course IDs",
        description = """
        Returns the list of course IDs the currently authenticated user is enrolled in.
        Requires a valid session cookie.
        """
    )
    @GetMapping(ApiPaths.PROGRESS.COURSES.ENROLLED)
    fun getEnrolledCourseIds(@AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<List<UUID>> {
        return ResponseEntity.ok(courseProgressService.getEnrolledCourseIds(userId))
    }

    @Operation(
        summary = "Reset course progress",
        description = """
        Resets the course progress for the specified course
        for the currently authenticated user.
        Returns the reset course progress state.
        Requires a valid session cookie to be present. 
        """
        )
    @PostMapping(ApiPaths.PROGRESS.COURSES.RESET)
    fun resetUserCourseProgress (@PathVariable courseId: UUID, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<CourseProgressResponse> {
        return ResponseEntity.ok(courseProgressService.resetUserCourseProgress(userId, courseId))
    }

    @Operation(
        summary = "Change active course",
        description = """
        Updates the active course for the authenticated user.
        Returns the course progress for the newly selected course.
        Requires a valid session cookie to be present.
        """
    )
    @PutMapping(ApiPaths.PROGRESS.COURSES.CURRENT)
    fun updateCurrentCourse(
        @RequestBody request: ChangeCourseRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<CourseProgressResponseWithEnrolled> {
        return ResponseEntity.ok(courseProgressService.findOrCreate(userId, request.newCourseId))
    }

}