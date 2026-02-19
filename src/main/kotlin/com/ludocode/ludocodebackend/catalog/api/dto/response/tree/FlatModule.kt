package com.ludocode.ludocodebackend.catalog.api.dto.response.tree

import java.util.*

data class FlatModule(
    val id: UUID,
    val orderIndex: Int,
    val lessons: List<FlatLesson>
)