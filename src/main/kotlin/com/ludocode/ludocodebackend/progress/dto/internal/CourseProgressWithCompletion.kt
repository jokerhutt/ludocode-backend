package com.ludocode.ludocodebackend.progress.dto.internal

import com.ludocode.ludocodebackend.progress.dto.response.CourseProgressResponse

data class CourseProgressWithCompletion(val courseProgressResponse: CourseProgressResponse, val isFirstCompletion: Boolean)
