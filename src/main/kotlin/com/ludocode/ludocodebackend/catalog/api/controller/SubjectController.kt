package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseSubjectResponse
import com.ludocode.ludocodebackend.catalog.app.service.SubjectService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.SUBJECTS.BASE)
class SubjectController(private val subjectService: SubjectService) {

    @GetMapping
    fun getSubjects () : ResponseEntity<List<CourseSubjectResponse>> {
        return ResponseEntity.ok(subjectService.getAllSubjects())
    }

}