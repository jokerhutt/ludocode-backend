package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.infra.repository.UserDailyGoalRepository
import com.ludocode.ludocodebackend.progress.infra.repository.UserStreakRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class StreakService(
    private val userStreakRepository: UserStreakRepository,
    private val userDailyGoalRepository: UserDailyGoalRepository
) {


    @Transactional
    fun getStreak(userId: UUID): UserStreak {
        userStreakRepository.initializeIfAbsent(userId)
        val s = userStreakRepository.findByUserId(userId)!!
        return s
    }

    @Transactional
    fun updateStreak(
        userId: UUID,
        nowUtc: OffsetDateTime,
        userZone: ZoneId
    ): UserStreak {
        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()

        val s = userStreakRepository.findByUserId(userId)  // may be null
        val last = s?.lastMetLocalDate

        val newCurrent =
            when {
                last == null -> 1
                today == last.plusDays(1) -> (s?.currentStreakDays ?: 0) + 1
                today.isAfter(last.plusDays(1)) -> 1
                else -> s?.currentStreakDays ?: 0
            }

        val newBest = maxOf(s?.bestStreakDays ?: 0, newCurrent)

        userStreakRepository.upsertProgress(
            userId = userId,
            current = newCurrent,
            best = newBest,
            lastLocal = today,
            lastUtc = nowUtc
        )

        return userStreakRepository.findByUserId(userId)!!
    }


    @Transactional
    fun recordGoalMet(userId: UUID, nowUtc: OffsetDateTime, userZone: ZoneId) {
        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()

        val inserted = userDailyGoalRepository.insertOnce(userId, today)
        if (inserted == 1) {
            updateStreak(userId, nowUtc, userZone)
        }
    }



}