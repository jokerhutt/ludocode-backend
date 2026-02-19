package com.ludocode.ludocodebackend.catalog.api.dto.response.tree

import java.util.*

data class FlatCourseTreeResponse(
    val courseId: UUID,
    val modules: List<FlatModule>
)
