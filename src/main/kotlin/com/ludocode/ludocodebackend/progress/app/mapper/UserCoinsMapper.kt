package com.ludocode.ludocodebackend.progress.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import org.springframework.stereotype.Component

@Component
class UserCoinsMapper (private val basicMapper: BasicMapper) {

    fun toUserCoinsResponse(userCoins: UserCoins): UserCoinsResponse {
        return basicMapper.one(userCoins) {
            UserCoinsResponse(
                id = userCoins.userId,
                coins = userCoins.coins,
            )
        }
    }

        fun toUserCoinsResponseList(userCoinsList: List<UserCoins>) : List<UserCoinsResponse> {
            return basicMapper.list(userCoinsList) { userStatsList ->
                toUserCoinsResponse(userStatsList)
            }
        }

}