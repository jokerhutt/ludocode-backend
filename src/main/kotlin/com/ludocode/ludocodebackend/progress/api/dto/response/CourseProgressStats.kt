package com.ludocode.ludocodebackend.progress.api.dto.response

import java.util.UUID

data class CourseProgressStats(val id: UUID, val totalLessons: Int, val completedLessons: Int)
