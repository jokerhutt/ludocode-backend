package com.ludocode.ludocodebackend.lesson.api.dto.response

import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import java.util.*

data class LessonResponse(
    val id: UUID,
    val title: String,
    val orderIndex: Int,
    val projectSnapshot: ProjectSnapshot?,
    var isCompleted: Boolean
)