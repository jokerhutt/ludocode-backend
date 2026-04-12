package com.ludocode.ludocodebackend.progress.api.dto.response

import java.time.LocalDate

data class DailyXpHistoryResponse (
    val date: LocalDate,
    val xp: Int
)