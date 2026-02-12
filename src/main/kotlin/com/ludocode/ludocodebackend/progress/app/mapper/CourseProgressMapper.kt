package com.ludocode.ludocodebackend.progress.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.infra.projection.CourseProgressWithModuleProjection
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CourseProgressMapper (private val basicMapper: BasicMapper) {

    fun toCourseProgressResponse(courseProgress: CourseProgress): CourseProgressResponse =
        basicMapper.one(courseProgress) {
            CourseProgressResponse(
                courseId = courseProgress.id.courseId,
                userId = courseProgress.id.userId,
                moduleId = courseProgress.currentModuleId,
                id = courseProgress.id.courseId,
                updatedAt = courseProgress.updatedAt.toInstant()
            )
        }

    fun toCourseProgressResponseList(courseProgressList: List<CourseProgress>): List<CourseProgressResponse> =
        basicMapper.list(courseProgressList) {courseProgress ->
            toCourseProgressResponse(courseProgress)
        }

    fun toCourseProgressResponseWithEnrolled(courseProgress: CourseProgress, enrolled: List<UUID>) : CourseProgressResponseWithEnrolled {
        return CourseProgressResponseWithEnrolled(toCourseProgressResponse(courseProgress), enrolled)
    }

}