package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import org.springframework.http.ResponseEntity
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


    @PostMapping(PathConstants.SUBMIT_SNAPSHOT)
    fun apply(@RequestBody s: ModuleSnapshot) : ResponseEntity<ModuleSnapshot> {
        return ResponseEntity.ok(snapshotService.applyModuleSnapshot(s))
    }

    @GetMapping(PathConstants.SNAPSHOTS_BY_COURSE)
    fun getSnapshotsByCourseId(@PathVariable courseId: UUID) : ResponseEntity<List<ModuleSnapshot>> {
        return ResponseEntity.ok(snapshotService.getSnapshotsByCourseId(courseId))
    }



}