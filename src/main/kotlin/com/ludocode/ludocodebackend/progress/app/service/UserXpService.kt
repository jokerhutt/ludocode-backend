package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.response.DailyXpHistoryResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.UserXpResponse
import com.ludocode.ludocodebackend.progress.app.mapper.UserXpMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserXpPortForAuth
import com.ludocode.ludocodebackend.progress.domain.entity.UserXp
import com.ludocode.ludocodebackend.progress.domain.entity.XpTransaction
import com.ludocode.ludocodebackend.progress.infra.repository.UserXpRepository
import com.ludocode.ludocodebackend.progress.infra.repository.XpTransactionRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UserXpService(
    private val userXpRepository: UserXpRepository,
    private val xpTransactionRepository: XpTransactionRepository,
    private val userXpMapper: UserXpMapper,
    private val clock: Clock
) : UserXpPortForAuth {

    private val logger = LoggerFactory.getLogger(UserXpService::class.java)

    companion object {
        private const val XP_NORMAL = 5
        private const val XP_PERFECT = 10
    }

    @Transactional
    override fun findOrCreateXp(userId: UUID): UserXpResponse {
        val stats = userXpRepository.findById(userId)
            .orElseGet {
                userXpRepository.save(UserXp(userId = userId, xp = 0))
            }
        return userXpMapper.toUserXpResponse(stats)
    }

    internal fun getUserXpList(userIds: List<UUID>): List<UserXpResponse> {
        return userXpMapper.toUserXpResponseList(userXpRepository.findAllById(userIds))
    }

    @Transactional
    internal fun applyLessonXp(userId: UUID, accuracy: BigDecimal): Pair<UserXpResponse, Int> {
        val isPerfect = accuracy.compareTo(BigDecimal.ONE) == 0
        val amount = if (isPerfect) XP_PERFECT else XP_NORMAL
        return Pair(apply(userId, amount), amount)
    }

    @Transactional
    internal fun apply(userId: UUID, amount: Int): UserXpResponse {
        val stats = userXpRepository.findById(userId)
            .orElseGet {
                userXpRepository.save(UserXp(userId = userId, xp = 0))
            }
        stats.xp += amount
        val newStats = userXpRepository.save(stats)

        xpTransactionRepository.save(
            XpTransaction(
                id = UUID.randomUUID(),
                userId = userId,
                amount = amount,
                balanceAfter = newStats.xp,
                createdAt = OffsetDateTime.now(clock)
            )
        )

        return userXpMapper.toUserXpResponse(newStats)
    }

    fun getXpHistory(userId: UUID, days: Int = 7): List<DailyXpHistoryResponse> {
        val today = LocalDate.now(clock)
        val startDate = today.minusDays(days.toLong() - 1)
        val allDays = (0 until days).map { startDate.plusDays(it.toLong()) }
        val cutoff = startDate.atStartOfDay().atOffset(OffsetDateTime.now(clock).offset)
        val transactions = xpTransactionRepository
            .findByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(userId, cutoff)

        val xpByDate = transactions
            .groupBy { it.createdAt.toLocalDate() }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        return allDays.map { date ->
            DailyXpHistoryResponse(date = date, xp = xpByDate[date] ?: 0)
        }
    }

}