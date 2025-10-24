package com.ludocode.ludocodebackend.progress.api.dto.internal

import com.ludocode.ludocodebackend.progress.domain.enums.StreakAction
import java.util.UUID

data class StatsDelta(
    val userId: UUID,
    val pointsDelta: Int = 0,
    val streakAction: StreakAction = StreakAction.NONE
)
