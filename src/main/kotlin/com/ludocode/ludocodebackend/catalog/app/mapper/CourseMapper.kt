package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import org.springframework.stereotype.Component

@Component
class CourseMapper (private val basicMapper: BasicMapper) {

    fun toCourseResponse(course: Course): CourseResponse =
        basicMapper.one(course) {
            CourseResponse(
                id = it.id!!,
                title = it.title!!
            )
        }

    fun toCourseResponseList(courses: List<Course>): List<CourseResponse> =
        basicMapper.list(courses) { course ->
            toCourseResponse(course)
        }


}