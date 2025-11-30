package com.ludocode.ludocodebackend.progress.dto.response

import java.util.UUID

class CourseProgressResponseWithEnrolled (val courseProgress: CourseProgressResponse, val enrolled: List<UUID>) {
}