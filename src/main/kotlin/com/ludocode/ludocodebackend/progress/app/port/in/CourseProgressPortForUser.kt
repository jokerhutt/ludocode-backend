package com.ludocode.ludocodebackend.progress.app.port.`in`

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import java.util.*

interface CourseProgressPortForUser {
    fun findOrCreate(userId: UUID, courseId: UUID): CourseProgressResponseWithEnrolled
    fun existsAnyByUserId(userId: UUID): Boolean
}