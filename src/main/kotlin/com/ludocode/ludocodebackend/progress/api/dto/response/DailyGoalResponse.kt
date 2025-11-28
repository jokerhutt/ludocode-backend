package com.ludocode.ludocodebackend.progress.api.dto.response

import java.time.LocalDate

data class DailyGoalResponse (val date: LocalDate, val met: Boolean)