package com.ludocode.ludocodebackend.catalog.api.dto.snapshot

import java.util.UUID

data class OptionSnap(
    val content: String,
    val answerOrder: Int?,
    val exerciseOptionId: UUID
)
