package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.SNAPSHOT)
class CatalogAdminController(
                             private val snapshotService: SnapshotService
) {

    @PostMapping(PathConstants.SUBMIT_COURSE_SNAPSHOT)
    fun applyCourseSnapshot(@RequestBody s: CourseSnap, @AuthenticationPrincipal (expression = "userId") userId: UUID) : ResponseEntity<CourseSnap> {
        return ResponseEntity.ok(snapshotService.applyNewSnapshot(s))
    }

    @GetMapping(PathConstants.GET_COURSE_SNAPSHOT)
    fun getSnapshotsByCourseId(@PathVariable courseId: UUID) : ResponseEntity<CourseSnap> {
        return ResponseEntity.ok(snapshotService.getCourseSnapshot(courseId))
    }

    @PostMapping(PathConstants.CREATE_COURSE)
    fun createCourse(@RequestBody request: CreateCourseRequest) : ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(snapshotService.createCourse(request))
    }


}