package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

class StreakIT : AbstractIntegrationTest() {

    @Test
    fun submitGetStreak_noStreakYet_returnsNew () {

        val userId = user1.id
        val userStats = userStatsRepository.save(UserStats(user1.id!!, 0, 0))
        val res = submitGetStreak(userId!!)

        assertThat(res).isNotNull()
        assertThat(res.current).isEqualTo(0)
        assertThat(res.best).isEqualTo(0)
        assertThat(res.lastMet).isNull()

    }

    @Test
    fun submitGetStreak_MissedStreak_returnsResetStreak () {

        val userStats = userStatsRepository.save(UserStats(user1.id!!, 0, 0))

        val lastStreak = UserStreak(userId = user1.id!!, currentStreakDays = 4, bestStreakDays = 7, lastMetGoalUtc = OffsetDateTime.now(clock).minusDays(2), lastMetLocalDate = LocalDate.now(clock).minusDays(2))
        userStreakRepository.save(lastStreak)

        val res = submitGetStreak(user1.id!!)

        assertThat(res).isNotNull()
        assertThat(res.current).isEqualTo(0)
        assertThat(res.best).isEqualTo(7)

    }

    private fun submitGetStreak (userId: UUID): UserStreakResponse {
        return given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .get("${PathConstants.STREAK}/get")
            .then()
            .statusCode(200)
            .extract()
            .`as`(UserStreakResponse::class.java)
    }
}