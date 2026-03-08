package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.tag.api.dto.TagMetadata
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CourseMapper(private val basicMapper: BasicMapper, private val languagesMapper: LanguagesMapper) {

    fun toCourseResponse(course: Course, tags: List<TagMetadata>): CourseResponse =
        basicMapper.one(course) {
            CourseResponse(
                id = it.id!!,
                title = it.title!!,
                courseType = it.courseType,
                courseIcon = it.courseIcon,
                language = it.language?.let { lang ->
                    languagesMapper.toLanguageMetadata(lang)
                },
                tags = tags,
                description = it.description,
                courseStatus = it.courseStatus
            )
        }

    fun toCourseResponseList(
        courses: List<Course>,
        tagsByCourse: Map<UUID, List<TagMetadata>>,
    ): List<CourseResponse> =
        basicMapper.list(courses) { course ->
            toCourseResponse(course, tagsByCourse[course.id] ?: emptyList())
        }

}