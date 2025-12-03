package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.internal.PointsDelta
import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import com.ludocode.ludocodebackend.progress.app.mapper.UserCoinsMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserCoinsPortForAuth
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.infra.repository.UserCoinsRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserCoinsService(private val userCoinsRepository: UserCoinsRepository,
                       private val userCoinsMapper: UserCoinsMapper
) : UserCoinsPortForAuth {



    @Transactional
    override fun findOrCreateCoins(userId: UUID): UserCoinsResponse {
        val stats = userCoinsRepository.findById(userId)
            .orElseGet {
                userCoinsRepository.save(UserCoins(userId = userId, coins = 0))
            }
        return userCoinsMapper.toUserCoinsResponse(stats)
    }


    internal fun getUserCoinsList (userIds: List<UUID>) : List<UserCoinsResponse> {
        return userCoinsMapper.toUserCoinsResponseList(userCoinsRepository.findAllById(userIds))
    }

    @Transactional
    internal fun apply(delta: PointsDelta): UserCoinsResponse {
        val stats = userCoinsRepository.findById(delta.userId).orElseGet {
            userCoinsRepository.save(UserCoins(delta.userId, 0))
        }
        stats.coins += delta.pointsDelta
        val newStats = userCoinsRepository.save(stats)
        return userCoinsMapper.toUserCoinsResponse(newStats)
    }

}