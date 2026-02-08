package com.ludocode.ludocodebackend.catalog.api.controller.admin

import com.ludocode.ludocodebackend.catalog.api.dto.request.SubjectRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseSubjectResponse
import com.ludocode.ludocodebackend.catalog.app.service.SubjectService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.SUBJECTS.ADMIN_BASE)
class SubjectAdminController(private val subjectService: SubjectService) {



    @PostMapping
    fun createSubject (@RequestBody req: SubjectRequest): ResponseEntity<List<CourseSubjectResponse>> {
        return ResponseEntity.ok(subjectService.createSubject(req))
    }

    @PutMapping(ApiPaths.SUBJECTS.BY_SUBJECT)
    fun updateSubject (@PathVariable subjectId: Long, @RequestBody req: SubjectRequest) : ResponseEntity<List<CourseSubjectResponse>> {
        return ResponseEntity.ok(subjectService.updateSubject(subjectId, req))
    }

    @DeleteMapping(ApiPaths.SUBJECTS.BY_SUBJECT)
    fun deleteSubject (@PathVariable subjectId: Long) : ResponseEntity<List<CourseSubjectResponse>> {
        return ResponseEntity.ok(subjectService.deleteSubject(subjectId))
    }



}