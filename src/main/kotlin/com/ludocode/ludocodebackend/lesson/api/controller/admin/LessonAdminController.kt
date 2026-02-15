package com.ludocode.ludocodebackend.lesson.api.controller.admin
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.lesson.app.service.admin.LessonSnapshotService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.LESSONS.ADMIN_BASE)
class LessonAdminController(
    private val lessonSnapshotService: LessonSnapshotService
) {

    @GetMapping(ApiPaths.LESSONS.BY_ID)
    fun getLessonCurriculumByCourseId(@PathVariable lessonId: UUID) : ResponseEntity<LessonCurriculumDraftSnapshot> {
        return ResponseEntity.ok(lessonSnapshotService.buildLessonCurriculumSnapshot(lessonId))
    }

    @PutMapping(ApiPaths.LESSONS.BY_ID)
    fun applyLessonCurriculumSnapshot(@RequestBody snapshot: LessonCurriculumDraftSnapshot, @PathVariable lessonId: UUID) : ResponseEntity<LessonCurriculumDraftSnapshot> {
        return ResponseEntity.ok(lessonSnapshotService.applyExerciseDiffs(lessonId, snapshot))
    }

}