package com.ludocode.ludocodebackend.progress.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStatsResponse
import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import org.springframework.stereotype.Component

@Component
class UserStatsMapper (private val basicMapper: BasicMapper) {

    fun toUserStatsResponse(userStats: UserStats): UserStatsResponse {
        return basicMapper.one(userStats) {
            UserStatsResponse(
                id = userStats.userId,
                coins = userStats.coins,
            )
        }
    }

        fun toUserStatsResponseList(userStatsList: List<UserStats>) : List<UserStatsResponse> {
            return basicMapper.list(userStatsList) { userStatsList ->
                toUserStatsResponse(userStatsList)
            }
        }

}