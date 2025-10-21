package com.ludocode.ludocodebackend.progress.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import org.springframework.stereotype.Component

@Component
class CourseProgressMapper (private val basicMapper: BasicMapper) {

    //TODO check this !! stuff for course progress

    fun toCourseProgressResponse(courseProgress: CourseProgress): CourseProgressResponse =
        basicMapper.one(courseProgress) {
            CourseProgressResponse(
                courseId = courseProgress.id.courseId,
                userId = courseProgress.id.userId,
                currentLessonId = courseProgress.currentLessonId!!
            )
        }

}