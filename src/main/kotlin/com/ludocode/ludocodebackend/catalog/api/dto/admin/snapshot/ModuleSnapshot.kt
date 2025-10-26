package com.ludocode.ludocodebackend.catalog.api.dto.admin.snapshot

import java.util.UUID

data class ModuleSnapshot(
    val moduleId: UUID,
    val title: String,
    val lessons: List<LessonSnap>
)