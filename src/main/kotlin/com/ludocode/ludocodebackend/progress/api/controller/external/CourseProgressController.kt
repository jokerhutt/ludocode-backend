package com.ludocode.ludocodebackend.progress.api.controller.external

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.app.service.CourseProgressService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.PROGRESS_COURSE)
class CourseProgressController(private val courseProgressService: CourseProgressService) {

    @GetMapping(PathConstants.USER_COURSE_PROGRESS)
    fun getCourseProgressList (@AuthenticationPrincipal userId: UUID): ResponseEntity<List<CourseProgressResponse>> {
        return ResponseEntity.ok(courseProgressService.findCourseProgressList(userId))
    }

}