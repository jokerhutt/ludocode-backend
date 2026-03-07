package com.ludocode.ludocodebackend.catalog.api.dto.response

import java.util.UUID

data class CourseTagMetadata (
    val courseId: UUID,
    val id: Long,
    val name: String,
    val slug: String
    )