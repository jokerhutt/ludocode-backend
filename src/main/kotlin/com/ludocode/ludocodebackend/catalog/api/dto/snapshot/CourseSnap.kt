package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import java.util.UUID

data class CourseSnap(
    val courseId: UUID,
    val title: String = "",
    val modules: List<ModuleSnapshot>
)
