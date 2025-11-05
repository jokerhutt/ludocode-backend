package com.ludocode.ludocodebackend.progress.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.infra.projection.CourseProgressWithModuleProjection
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CourseProgressMapper (private val basicMapper: BasicMapper) {

    //TODO check this !! stuff for course progress

    fun toCourseProgressResponse(courseProgress: CourseProgressWithModuleProjection): CourseProgressResponse =
        basicMapper.one(courseProgress) {
            CourseProgressResponse(
                courseId = courseProgress.getCourseId(),
                userId = courseProgress.getUserId(),
                currentLessonId = courseProgress.getCurrentLessonId(),
                moduleId = courseProgress.getModuleId()!!,
                id = courseProgress.getCourseId(),
                updatedAt = courseProgress.getUpdatedAt()
            )
        }

    fun toCourseProgressResponseList(courseProgressList: List<CourseProgressWithModuleProjection>): List<CourseProgressResponse> =
        basicMapper.list(courseProgressList) {courseProgress ->
            toCourseProgressResponse(courseProgress)
        }

    fun toCourseProgressResponseWithEnrolled(courseProgress: CourseProgressWithModuleProjection, enrolled: List<UUID>) : CourseProgressResponseWithEnrolled {
        return CourseProgressResponseWithEnrolled(toCourseProgressResponse(courseProgress), enrolled)
    }

}