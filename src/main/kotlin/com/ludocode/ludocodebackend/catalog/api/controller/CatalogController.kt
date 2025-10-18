package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.app.service.CatalogService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(PathConstants.CATALOG)
class CatalogController(private val catalogService: CatalogService) {

    @GetMapping(PathConstants.COURSES_ALL)
    fun getAllCourses(userId: Int): ResponseEntity<List<CourseResponse>> {
        return ResponseEntity.ok(catalogService.getAllCourses())
    }





}