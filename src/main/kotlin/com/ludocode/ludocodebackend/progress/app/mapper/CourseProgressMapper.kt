package com.ludocode.ludocodebackend.progress.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.infra.projection.CourseProgressWithModuleProjection
import org.springframework.stereotype.Component

@Component
class CourseProgressMapper (private val basicMapper: BasicMapper) {

    //TODO check this !! stuff for course progress

    fun toCourseProgressResponse(courseProgress: CourseProgressWithModuleProjection): CourseProgressResponse =
        basicMapper.one(courseProgress) {
            CourseProgressResponse(
                courseId = courseProgress.getCourseId(),
                userId = courseProgress.getUserId(),
                currentLessonId = courseProgress.getCurrentLessonId(),
                moduleId = courseProgress.getModuleId()!!
            )
        }

    fun toCourseProgressResponseList(courseProgressList: List<CourseProgressWithModuleProjection>): List<CourseProgressResponse> =
        basicMapper.list(courseProgressList) {courseProgress ->
            toCourseProgressResponse(courseProgress)
        }

}