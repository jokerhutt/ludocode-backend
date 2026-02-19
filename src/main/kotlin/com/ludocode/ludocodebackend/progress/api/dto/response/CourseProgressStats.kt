package com.ludocode.ludocodebackend.progress.api.dto.response

import java.util.*

data class CourseProgressStats(val id: UUID, val totalLessons: Int, val completedLessons: Int)
