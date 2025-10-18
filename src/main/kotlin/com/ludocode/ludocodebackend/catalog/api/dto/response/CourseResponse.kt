package com.ludocode.ludocodebackend.catalog.api.dto.response

import java.util.UUID

data class CourseResponse(
    val id: UUID,
    val title: String,
)
