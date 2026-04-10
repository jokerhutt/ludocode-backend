package com.ludocode.ludocodebackend.preferences.api.dto.response

import java.util.UUID

data class CareerResponse(
    val id: UUID,
    val title: String,
    val choice: String,
    val description: String,
    val defaultCourseId: UUID
)