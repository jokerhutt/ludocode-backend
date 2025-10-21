package com.ludocode.ludocodebackend.progress.app.port.`in`

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import java.util.UUID

interface CourseProgressUseCase {

    fun findOrCreate(userId: UUID, courseId: UUID): CourseProgressResponse

}