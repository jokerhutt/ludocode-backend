package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.app.mapper.UserStreakMapper
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
    private val userStreakMapper: UserStreakMapper
) {


    @Transactional
    fun getStreak(userId: UUID): UserStreakResponse {
        userStreakRepository.initializeIfAbsent(userId)
        val userStreak = userStreakRepository.findByUserId(userId)!!
        return userStreakMapper.toStreakResponse(userStreak)
    }

    @Transactional
    fun updateStreak(
        userId: UUID,
        nowUtc: OffsetDateTime,
        userZone: ZoneId
    ): UserStreak {

        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()

        val userStreak = userStreakRepository.findByUserId(userId)
        val lastMetDate = userStreak?.lastMetLocalDate

        val newCurrent = calculateNewStreak(today, lastMetDate, userStreak!!.currentStreakDays!!)

        val newBest = maxOf(userStreak?.bestStreakDays ?: 0, newCurrent)

        userStreakRepository.upsertProgress(
            userId = userId,
            current = newCurrent,
            best = newBest,
            lastLocal = today,
            lastUtc = nowUtc
        )

        return userStreakRepository.findByUserId(userId)!!
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
    fun recordGoalMet(userId: UUID, nowUtc: OffsetDateTime, userZone: ZoneId) {
        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()

        val inserted = userDailyGoalRepository.insertOnce(userId, today)
        if (inserted == 1) {
            updateStreak(userId, nowUtc, userZone)
        }
    }



}