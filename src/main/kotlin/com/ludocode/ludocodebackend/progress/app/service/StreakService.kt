package com.ludocode.ludocodebackend.progress.app.service
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.progress.api.dto.response.DailyGoalResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.StreakResponsePacket
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.app.mapper.UserStreakMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserStreakPortForAuth
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForProgress
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.domain.enums.StreakAction
import com.ludocode.ludocodebackend.progress.infra.repository.UserDailyGoalRepository
import com.ludocode.ludocodebackend.progress.infra.repository.UserStreakRepository
import java.time.Clock
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlin.math.max

@Service
class StreakService(
    private val userStreakRepository: UserStreakRepository,
    private val userDailyGoalRepository: UserDailyGoalRepository,
    private val userStreakMapper: UserStreakMapper,
    private val userPortForProgress: UserPortForProgress,
    private val clock: Clock,
) : UserStreakPortForAuth {


    @Transactional
    override fun getStreak(userId: UUID): UserStreakResponse {
        val userStreak = initializeIfAbsentReturning(userId)
        return userStreakMapper.toStreakResponse(userStreak)
    }

    @Transactional
    internal fun recordGoalMet(userId: UUID, nowUtc: OffsetDateTime): StreakResponsePacket {
        val userZone = ZoneId.of(userPortForProgress.getUserTimezone(userId))
        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()
        initializeIfAbsentReturning(userId)
        val inserted = userDailyGoalRepository.insertOnce(userId, today)
        if (inserted == 0) return StreakResponsePacket(action = StreakAction.NONE, response = getStreak(userId))
        return StreakResponsePacket(action = StreakAction.INCREMENT, response = updateStreak(userId, nowUtc, userZone))
    }


    internal fun getPastWeekMondayToSunday(
        userId: UUID,
    ): List<DailyGoalResponse> {

        val today = LocalDate.now(clock)

        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val week = (0..6).map { monday.plusDays(it.toLong()) }

        val completions = userDailyGoalRepository.findRecentCompletions(userId, 7)
        val completedDates = completions.map { it.userDailyGoalId.localDate }.toSet()

        return week.map { date ->
            DailyGoalResponse(date, completedDates.contains(date))
        }
    }

    private fun initializeIfAbsentReturning(userId: UUID): UserStreak {
        val streak = userStreakRepository.findByUserId(userId)
        if (streak == null) {
            return userStreakRepository.save(createNewStreak(userId))
        }
        if (shouldResetStreak(userId, streak.lastMetLocalDate)) {
           streak.currentStreakDays = 0
           userStreakRepository.save(streak)
        }
        return streak
    }

    private fun upsertStreakInRepository(
        userId: UUID,
        current: Int,
        best: Int,
        lastLocal: LocalDate,
        lastUtc: OffsetDateTime
    ) {
        val existing = userStreakRepository.findById(userId).orElse(createNewStreak(userId))
        existing.currentStreakDays = current
        existing.bestStreakDays = max(existing.bestStreakDays ?: 0, best)
        existing.lastMetLocalDate = lastLocal
        existing.lastMetGoalUtc = lastUtc
        userStreakRepository.save(existing)
    }

    private fun updateStreak(
        userId: UUID,
        nowUtc: OffsetDateTime,
        userZone: ZoneId
    ): UserStreakResponse {
        val current = initializeIfAbsentReturning(userId)
        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()
        val lastMet = current.lastMetLocalDate
        val currentDays = current.currentStreakDays ?: 0
        val bestDays = current.bestStreakDays ?: 0
        val newCurrent = calculateNewStreak(today, lastMet, currentDays)
        val newBest = maxOf(bestDays, newCurrent)

        upsertStreakInRepository(
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

    private fun shouldResetStreak (userId: UUID, lastModified: LocalDate?) : Boolean {
        if (lastModified == null) return false;
        val userZone = ZoneId.of(userPortForProgress.getUserTimezone(userId))
        val nowUtc = OffsetDateTime.now(clock)
        val today = nowUtc.atZoneSameInstant(userZone).toLocalDate()
        return hasMissedStreak(today, lastModified)
    }

    private fun createNewStreak (userId: UUID) : UserStreak {
        return UserStreak(
            userId = userId,
            currentStreakDays = 0,
            bestStreakDays = 0,
            lastMetLocalDate = null,
            lastMetGoalUtc = null
        )
    }



    private fun isFirstEverSubmission (lastModified: LocalDate?) : Boolean = lastModified == null
    private fun isFirstSubmissionOfDay (today: LocalDate, lastModified: LocalDate) : Boolean = today == lastModified.plusDays(1)
    private fun hasMissedStreak (today: LocalDate, lastModified: LocalDate) : Boolean = today.isAfter(lastModified.plusDays(1))

}