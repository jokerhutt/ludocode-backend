package com.ludocode.ludocodebackend.user.app.port.out

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import java.util.UUID

interface CourseProgressPortForUser {

    fun findOrCreate(userId: UUID, courseId: UUID): CourseProgressResponseWithEnrolled

}