package com.ludocode.ludocodebackend.lesson.app.port.`in`

import com.ludocode.ludocodebackend.lesson.api.dto.response.LessonResponse
import java.util.UUID

interface LessonPortForProgress {
    fun findLessonResponseById(lessonId: UUID, userId: UUID): LessonResponse
}