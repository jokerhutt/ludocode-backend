package com.ludocode.ludocodebackend.lesson.api.dto.snapshot

import java.util.*

data class OptionSnap(
    val content: String,
    val answerOrder: Int?,
    val exerciseOptionId: UUID
)