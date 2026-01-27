package com.ludocode.ludocodebackend.progress.api.dto.response

import java.util.UUID

data class CourseProgressStats(val courseId: UUID, val totalLessons: Int, val completedLessons: Int)
