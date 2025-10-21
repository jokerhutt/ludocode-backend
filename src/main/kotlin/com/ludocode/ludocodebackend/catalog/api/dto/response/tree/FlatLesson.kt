package com.ludocode.ludocodebackend.catalog.api.dto.response.tree

import java.util.UUID

data class FlatLesson(
    val id: UUID,
    val orderIndex: Int
)
