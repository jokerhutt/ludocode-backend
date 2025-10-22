package com.ludocode.ludocodebackend.user.api.dto.response

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import java.util.UUID

data class UpdatedCourseResponse(val user: UserResponse, val courseProgress: CourseProgressResponse, val enrolled: List<UUID>)
