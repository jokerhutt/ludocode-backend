package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import java.util.UUID

data class ModuleSnapshot(
    val moduleId: UUID,
    val tempId: UUID,
    val title: String,
    val lessons: List<LessonSnap>
)