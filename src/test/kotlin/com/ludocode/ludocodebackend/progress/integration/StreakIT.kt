package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.DailyGoalResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.domain.entity.UserDailyGoal
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.UserDailyGoalId
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestClocks
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.Test

class StreakIT : AbstractIntegrationTest() {

    @Test
    fun submitGetStreak_noStreakYet_returnsNew () {

        val userId = user1.id
        val userCoins = userCoinsRepository.save(UserCoins(user1.id!!, 0))
        val res = submitGetStreak(userId!!)

        assertThat(res).isNotNull()
        assertThat(res.current).isEqualTo(0)
        assertThat(res.best).isEqualTo(0)
        assertThat(res.lastMet).isNull()

    }

    @Test
    fun submitGetStreakHistory_threeDaysMissing_returnsCorrect() {
        val userId = user1.id!!
        userCoinsRepository.save(UserCoins(userId, 0))

        clock.set(TestClocks.FIXED_NOON_UTC_MONDAY.instant())
        val missingDays = setOf(3, 4, 6)

        for (i in 0 until 7) {
            val today = LocalDate.now(clock)

            if (i !in missingDays) {
                userDailyGoalRepository.save(UserDailyGoal(UserDailyGoalId(userId, today)))
            }

            clock.set(clock.instant().plus(1, ChronoUnit.DAYS))
        }

        clock.set(clock.instant().minus(1, ChronoUnit.DAYS))

        val res = submitGetPastStreakWeek(userId)

        assertThat(res).hasSize(7)
        assertThat(res.count { it.met }).isEqualTo(4)
    }

    @Test
    fun submitGetStreakHistory_completedLastWeek_isMondayOfNextWeek_returnsEmpty() {
        val userId = user1.id!!
        userCoinsRepository.save(UserCoins(userId, 0))

        clock.set(TestClocks.FIXED_NOON_UTC_MONDAY.instant())

        for (i in 0 until 7) {
            val today = LocalDate.now(clock)
            userDailyGoalRepository.save(UserDailyGoal(UserDailyGoalId(userId, today)))
            clock.set(clock.instant().plus(1, ChronoUnit.DAYS))
        }

        val res = submitGetPastStreakWeek(userId)

        assertThat(res).hasSize(7)
        assertThat(res.count { it.met }).isEqualTo(0)
    }



    @Test
    fun submitGetStreak_MissedStreak_returnsResetStreak () {

        val userCoins = userCoinsRepository.save(UserCoins(user1.id!!, 0))

        val lastStreak = UserStreak(userId = user1.id!!, currentStreakDays = 4, bestStreakDays = 7, lastMetGoalUtc = OffsetDateTime.now(clock).minusDays(2), lastMetLocalDate = LocalDate.now(clock).minusDays(2))
        userStreakRepository.save(lastStreak)

        val res = submitGetStreak(user1.id!!)

        assertThat(res).isNotNull()
        assertThat(res.current).isEqualTo(0)
        assertThat(res.best).isEqualTo(7)

    }

    private fun submitGetStreak (userId: UUID): UserStreakResponse =
        TestRestClient.getOk("${PathConstants.STREAK}/get", userId, UserStreakResponse::class.java)

    private fun submitGetPastStreakWeek (userId: UUID) : List<DailyGoalResponse> =
        TestRestClient
            .getOk("${PathConstants.STREAK}${PathConstants.GET_STREAK_PAST_WEEK}",
                user1.id!!,
                Array<DailyGoalResponse>::class.java)
            .toList()

}
