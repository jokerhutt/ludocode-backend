package com.ludocode.ludocodebackend.progress.api.dto.internal

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse

data class CourseProgressWithCompletion(val courseProgressResponse: CourseProgressResponse, val isFirstCompletion: Boolean)
