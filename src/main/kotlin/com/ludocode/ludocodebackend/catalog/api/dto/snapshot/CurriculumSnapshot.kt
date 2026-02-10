package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import java.util.UUID

data class CurriculumDraftSnapshot(
    var modules: List<ModuleDraftSnapshot>
)

data class ModuleDraftSnapshot(
    val id: UUID,
    val title: String,
    var lessons: List<LessonDraftSnapshot>,
)

data class LessonDraftSnapshot(
    val id: UUID,
    var title: String,
)