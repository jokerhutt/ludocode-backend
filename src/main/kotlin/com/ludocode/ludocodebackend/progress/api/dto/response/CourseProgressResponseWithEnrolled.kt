package com.ludocode.ludocodebackend.progress.api.dto.response

import java.util.*

class CourseProgressResponseWithEnrolled(val courseProgress: CourseProgressResponse, val enrolled: List<UUID>)