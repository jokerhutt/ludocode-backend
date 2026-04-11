package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.progress.api.dto.internal.PointsDelta
import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import com.ludocode.ludocodebackend.progress.app.mapper.UserCoinsMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserCoinsPortForAuth
import com.ludocode.ludocodebackend.progress.domain.entity.CoinTransaction
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.domain.enums.CoinTransactionType
import com.ludocode.ludocodebackend.progress.infra.repository.CoinTransactionRepository
import com.ludocode.ludocodebackend.progress.infra.repository.UserCoinsRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

@Service
class UserCoinsService(
    private val userCoinsRepository: UserCoinsRepository,
    private val coinTransactionRepository: CoinTransactionRepository,
    private val userCoinsMapper: UserCoinsMapper,
    private val clock: Clock
) : UserCoinsPortForAuth {

    private val logger = LoggerFactory.getLogger(UserCoinsService::class.java)

    @Transactional
    override fun findOrCreateCoins(userId: UUID): UserCoinsResponse {
        val stats = userCoinsRepository.findById(userId)
            .orElseGet {
                userCoinsRepository.save(UserCoins(userId = userId, coins = 0))
            }
        return userCoinsMapper.toUserCoinsResponse(stats)
    }

    internal fun getUserCoinsList(userIds: List<UUID>): List<UserCoinsResponse> {
        return userCoinsMapper.toUserCoinsResponseList(userCoinsRepository.findAllById(userIds))
    }

    @Transactional
    internal fun apply(delta: PointsDelta): UserCoinsResponse {
        val stats = userCoinsRepository.findByUserIdForUpdate(delta.userId)
            ?: userCoinsRepository.save(UserCoins(delta.userId, 0))
        val oldCoins = stats.coins
        stats.coins += delta.pointsDelta
        val newStats = userCoinsRepository.save(stats)

        coinTransactionRepository.save(
            CoinTransaction(
                id = UUID.randomUUID(),
                userId = delta.userId,
                amount = delta.pointsDelta,
                balanceAfter = newStats.coins,
                transactionType = delta.transactionType,
                referenceId = delta.referenceId,
                createdAt = OffsetDateTime.now(clock)
            )
        )

        logger.info(
            LogEvents.USER_COINS_ADJUSTED + " {} {} {}",
            kv(LogFields.DELTA, delta.pointsDelta),
            kv(LogFields.OLD_COINS, oldCoins),
            kv(LogFields.NEW_COINS, newStats.coins),
        )
        return userCoinsMapper.toUserCoinsResponse(newStats)
    }

    @Transactional
    internal fun removeCoins(userId: UUID, amount: Int, transactionType: CoinTransactionType, referenceId: UUID? = null): UserCoinsResponse {
        return apply(
            PointsDelta(
                userId = userId,
                pointsDelta = -amount,
                transactionType = transactionType,
                referenceId = referenceId
            )
        )
    }

}