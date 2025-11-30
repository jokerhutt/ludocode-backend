package com.ludocode.ludocodebackend.progress.dto.response

import java.time.LocalDate

data class UserStreakResponse(val current: Int, val best: Int, val lastMet: LocalDate?)
