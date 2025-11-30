package com.ludocode.ludocodebackend.progress.dto.internal

import java.util.UUID

data class PointsDelta(
    val userId: UUID,
    val pointsDelta: Int = 0,
)
