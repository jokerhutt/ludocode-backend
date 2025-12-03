package com.ludocode.ludocodebackend.progress.api.dto.response

import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus


data class LessonCompletionPacket(
    val content: LessonCompletionResponse?,
    val status: LessonCompletionStatus
)
