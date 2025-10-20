package com.ludocode.ludocodebackend.catalog.api.dto.response

import java.util.UUID

data class CourseTreeResponse(
    val course: CourseResponse,
    val modules: List<ModuleNodeResponse>
)