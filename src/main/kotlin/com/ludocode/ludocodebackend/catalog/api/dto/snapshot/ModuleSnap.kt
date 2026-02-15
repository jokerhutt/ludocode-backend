package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.LessonSnap
import java.util.UUID

data class ModuleSnap(
    val moduleId: UUID,
    val title: String,
    val lessons: List<LessonSnap>
)