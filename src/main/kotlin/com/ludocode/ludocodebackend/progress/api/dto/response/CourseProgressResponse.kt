package com.ludocode.ludocodebackend.progress.api.dto.response

import java.util.UUID

data class CourseProgressResponse(val userId: UUID, val courseId: UUID, val currentLessonId: UUID, val moduleId: UUID)
