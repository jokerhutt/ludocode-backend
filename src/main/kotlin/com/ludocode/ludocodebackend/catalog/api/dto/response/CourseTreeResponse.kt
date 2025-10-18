package com.ludocode.ludocodebackend.catalog.api.dto.response

import java.util.UUID

data class CourseTreeResponse(
    val id: UUID,
    val title: String,
    val modules: List<ModuleNodeResponse>
)