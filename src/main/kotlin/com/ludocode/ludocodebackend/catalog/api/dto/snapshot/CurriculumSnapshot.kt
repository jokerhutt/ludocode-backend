package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import java.util.UUID

data class CurriculumDraftSnapshot(
    val modules: List<ModuleDraftSnapshot>
)

data class ModuleDraftSnapshot(
    val id: UUID,
    val title: String,
    val lessons: List<LessonDraftSnapshot>,
)

data class LessonDraftSnapshot(
    val id: UUID,
    val title: String,
)