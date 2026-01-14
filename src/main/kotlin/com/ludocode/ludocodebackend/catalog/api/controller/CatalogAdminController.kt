package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("admin", "test")
@RestController
@RequestMapping(ApiPaths.SNAPSHOTS.BASE)
class CatalogAdminController(
                             private val snapshotService: SnapshotService
) {

    @PutMapping(ApiPaths.SNAPSHOTS.BY_COURSE)
    fun applyCourseSnapshot(@RequestBody s: CourseSnap, @PathVariable courseId: UUID, @AuthenticationPrincipal (expression = "userId") userId: UUID) : ResponseEntity<CourseSnap> {
        return ResponseEntity.ok(snapshotService.applyNewSnapshot(s))
    }

    @GetMapping(ApiPaths.SNAPSHOTS.BY_COURSE)
    fun getSnapshotsByCourseId(@PathVariable courseId: UUID) : ResponseEntity<CourseSnap> {
        return ResponseEntity.ok(snapshotService.getCourseSnapshot(courseId))
    }

    @PostMapping(ApiPaths.SNAPSHOTS.COURSE)
    fun createCourse(@RequestBody request: CreateCourseRequest) : ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(snapshotService.createCourse(request))
    }


}