package com.ludocode.ludocodebackend.progress.api.dto.response

import com.ludocode.ludocodebackend.progress.domain.enums.StreakAction

data class StreakResponsePacket(val action: StreakAction, val response: UserStreakResponse)
