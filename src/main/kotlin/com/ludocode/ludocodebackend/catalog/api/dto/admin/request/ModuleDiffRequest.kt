package com.ludocode.ludocodebackend.catalog.api.dto.admin.request

import java.util.UUID

data class ModuleDiffRequest(
    val moduleId: UUID,
    val title: String,
    val orderByIds: List<UUID>,
    val changedLessons: List<LessonDiffRequest>,
    val lessonsToDelete: List<UUID>
)
