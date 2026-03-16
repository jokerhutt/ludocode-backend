package com.ludocode.ludocodebackend.lesson.api.dto.snapshot

import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import java.util.*

data class LessonSnap(
    val id: UUID,
    var title: String,
    var lessonType: LessonType = LessonType.NORMAL,
    var projectSnapshot: ProjectSnapshot? = null,
    val orderIndex: Int,
    val exercises: List<ExerciseSnap>
)