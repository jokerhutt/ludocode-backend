package com.ludocode.ludocodebackend.lesson.api.dto.response

import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import java.util.*

data class LessonResponse(
    val id: UUID,
    val title: String,
    val orderIndex: Int,
    val lessonType: LessonType,
    val projectSnapshot: ProjectSnapshot?,
    var isCompleted: Boolean
)