package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseSubjectResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.tag.domain.entity.Subject
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import org.springframework.stereotype.Component

@Component
class CourseMapper(private val basicMapper: BasicMapper, private val languagesMapper: LanguagesMapper) {

    fun toCourseResponse(course: Course): CourseResponse =
        basicMapper.one(course) {
            CourseResponse(
                id = it.id!!,
                title = it.title!!,
                courseType = it.courseType,
                courseIcon = it.courseIcon,
                language = it.language?.let { lang ->
                    languagesMapper.toLanguageMetadata(lang)
                },
                description = it.description
            )
        }

    fun toCourseResponseList(courses: List<Course>): List<CourseResponse> =
        basicMapper.list(courses) { course ->
            toCourseResponse(course)
        }

    fun toCourseSubjectResponse(subject: Subject): CourseSubjectResponse {
        return CourseSubjectResponse(
            subjectId = subject.id,
            slug = subject.slug,
            name = subject.name,
        )
    }

}