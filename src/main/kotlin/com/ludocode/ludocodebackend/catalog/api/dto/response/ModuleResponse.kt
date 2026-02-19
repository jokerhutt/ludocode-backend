package com.ludocode.ludocodebackend.catalog.api.dto.response

import java.util.*

data class ModuleResponse(
    val id: UUID,
    val title: String,
    val courseId: UUID,
    val orderIndex: Int
)
