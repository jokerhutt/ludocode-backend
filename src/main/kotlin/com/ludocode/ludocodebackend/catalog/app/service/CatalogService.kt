package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import org.springframework.stereotype.Service

@Service
class CatalogService(private val courseMapper: CourseMapper, private val courseRepository: CourseRepository) {

    fun getAllCourses (): List<CourseResponse> {
        return courseMapper.toCourseResponseList(courseRepository.findAll())
    }

}