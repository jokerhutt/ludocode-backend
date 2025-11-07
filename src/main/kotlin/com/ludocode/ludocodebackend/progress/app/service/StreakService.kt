package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.app.mapper.UserStreakMapper
import com.ludocode.ludocodebackend.progress.app.port.out.UserPortForProgress
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.infra.repository.UserDailyGoalRepository
import com.ludocode.ludocodebackend.progress.infra.repository.UserStreakRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class StreakService(
    private val userStreakRepository: UserStreakRepository,
    private val userDailyGoalRepository: UserDailyGoalRepository,
    private val userStreakMapper: UserStreakMapper,
    private val userPortForProgress: UserPortForProgress
) {


    @Transactional
    fun getStreak(userId: UUID): UserStreakResponse {
        val userStreak = userStreakRepository.initializeIfAbsentReturning(userId)
        return userStreakMapper.toStreakResponse(userStreak)
    }

    @Transactional
    fun updateStreak(
        userId: UUID,
        nowUtc: OffsetDateTime,
        userZone: ZoneId
    ): UserStreakResponse {
        userStreakRepository.initializeIfAbsentReturning(userId)

        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()

        // lock current row to compute deterministically
        val current = userStreakRepository.getForUpdate(userId) ?: UserStreak(userId = userId)
        val lastMet = current.lastMetLocalDate
        val currentDays = current.currentStreakDays ?: 0
        val bestDays = current.bestStreakDays ?: 0

        val newCurrent = calculateNewStreak(today, lastMet, currentDays)
        val newBest = maxOf(bestDays, newCurrent)

        userStreakRepository.upsertProgress(
            userId = userId,
            current = newCurrent,
            best = newBest,
            lastLocal = today,
            lastUtc = nowUtc
        )

        return getStreak(userId)
    }

    private fun calculateNewStreak (today: LocalDate, lastModified: LocalDate? ,currentStreakDays: Int): Int {
        if (isFirstEverSubmission(lastModified)) return 1
        if (isFirstSubmissionOfDay(today, lastModified!!)) return currentStreakDays + 1
        if (hasMissedStreak(today, lastModified!!)) return 1
        return currentStreakDays
    }

    private fun isFirstEverSubmission (lastModified: LocalDate?) : Boolean = lastModified == null
    private fun isFirstSubmissionOfDay (today: LocalDate, lastModified: LocalDate) : Boolean = today == lastModified.plusDays(1)
    private fun hasMissedStreak (today: LocalDate, lastModified: LocalDate) : Boolean = today.isAfter(lastModified.plusDays(1))


    @Transactional
    fun recordGoalMet(userId: UUID, nowUtc: OffsetDateTime): UserStreakResponse {
        val tz = userPortForProgress.getUserTimezone(userId)
        val userZone = try { ZoneId.of(tz) } catch (_: Exception) { ZoneId.of("UTC") }

        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()

        userStreakRepository.initializeIfAbsentReturning(userId)

        val inserted = userDailyGoalRepository.insertOnce(userId, today)
        if (inserted == 0) return getStreak(userId)

        return updateStreak(userId, nowUtc, userZone)
    }



}