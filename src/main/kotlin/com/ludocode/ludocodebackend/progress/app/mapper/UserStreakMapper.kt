package com.ludocode.ludocodebackend.progress.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.infra.projection.UserStreakRow
import org.springframework.stereotype.Component

@Component
class UserStreakMapper (private val basicMapper: BasicMapper) {

    fun toStreakResponse (userStreak: UserStreak) : UserStreakResponse {

        return basicMapper.one(userStreak) {
            UserStreakResponse(
                current = userStreak.currentStreakDays!!,
                best = userStreak.bestStreakDays!!,
                lastMet = userStreak.lastMetLocalDate
            )
        }
    }


}