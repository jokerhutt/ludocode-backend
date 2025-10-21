package com.ludocode.ludocodebackend.user.api.dto.response

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse

data class UpdatedCourseResponse(val user: UserResponse, val courseProgess: CourseProgressResponse)
