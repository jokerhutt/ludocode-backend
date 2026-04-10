package com.ludocode.ludocodebackend.progress.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.progress.api.dto.response.UserXpResponse
import com.ludocode.ludocodebackend.progress.domain.entity.UserXp
import org.springframework.stereotype.Component

@Component
class UserXpMapper (private val basicMapper: BasicMapper){
    fun toUserXpResponse(userXp: UserXp): UserXpResponse {
        return basicMapper.one(userXp) {
            UserXpResponse(
                id = userXp.userId,
                xp = userXp.xp,
            )
        }
    }

    fun toUserXpResponseList(userXpList: List<UserXp>): List<UserXpResponse> {
        return basicMapper.list(userXpList) { userStatsList ->
            toUserXpResponse(userStatsList)
        }
    }
}