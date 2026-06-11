package com.ludocode.ludocodebackend.leaderboard.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.leaderboard.api.dto.WeeklyLeaderboardResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.DailyXpHistoryResponse
import com.ludocode.ludocodebackend.progress.domain.entity.XpTransaction
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestClocks
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class LeaderboardServiceIT : AbstractIntegrationTest() {

    @Test
    fun getWeeklyLeaderboard_twoUsers_returnsTwoUsersCorrectlyRanked() {
        val user1Id = user1.id
        val user2Id = user2.id

        clock.set(TestClocks.FIXED_NOON_UTC_WEDNESDAY.instant())
        val now = OffsetDateTime.now(clock)

        // User 1 has 20 pts, User 2 has 10

        xpTransactionRepository.save(
            XpTransaction(userId = user1Id, amount = 10, balanceAfter = 10, createdAt = now.minusHours(4))
        )

        xpTransactionRepository.save(
            XpTransaction(userId = user1Id, amount = 10, balanceAfter = 20, createdAt = now.minusDays(1))
        )

        xpTransactionRepository.save(
            XpTransaction(userId = user2Id, amount = 10, balanceAfter = 10, createdAt = now.minusDays(1))
        )

        val res = submitGetWeeklyLeaderboardHistory()

        assertThat(res.leaderboardUsers).isNotEmpty()
        assertThat(res.leaderboardUsers).hasSize(2)

        val topRanked = res.leaderboardUsers[0]
        val secondRanked = res.leaderboardUsers[1]

        assertThat(topRanked.rank).isEqualTo(1)
        assertThat(topRanked.userId).isEqualTo(user1Id)
        assertThat(topRanked.xp).isEqualTo(20)
        assertThat(secondRanked.rank).isEqualTo(2)
        assertThat(secondRanked.userId).isEqualTo(user2Id)
        assertThat(secondRanked.xp).isEqualTo(10)

    }

    // do we get 7 days mon - sun
    @Test
    fun getWeeklyLeaderboardHistory_daysIsSeven() {

        // It is Wednesday my dudes
        clock.set(TestClocks.FIXED_NOON_UTC_WEDNESDAY.instant())

        val res = submitGetWeeklyLeaderboardHistory()

        assertThat(
            ChronoUnit.DAYS.between(
                res.startDate,
                res.endDate
            ) + 1
        ).isEqualTo(7)
    }


    // are last weeks excluded
    @Test
    fun getWeeklyLeaderboardHistory_returnsOnlyCurrentWeekXp() {

        clock.set(TestClocks.FIXED_NOON_UTC_WEDNESDAY.instant())
        val now = OffsetDateTime.now(clock)
        val userId = user1.id!!

        xpTransactionRepository.save(
            XpTransaction(userId = userId, amount = 10, balanceAfter = 10, createdAt = now.minusDays(4))
        )

        xpTransactionRepository.save(
            XpTransaction(userId = userId, amount = 10, balanceAfter = 20, createdAt = now.minusDays(1))
        )

        xpTransactionRepository.save(
            XpTransaction(userId = userId, amount = 5, balanceAfter = 25, createdAt = now.minusHours(1))
        )

        val res = submitGetWeeklyLeaderboardHistory()

        assertThat(res.leaderboardUsers).isNotEmpty()
        assertThat(res.leaderboardUsers).hasSize(1) // since only 1 user for this test

        val userLeaderboardRow = res.leaderboardUsers[0]
        assertThat(userLeaderboardRow.userId).isEqualTo(userId)
        assertThat(userLeaderboardRow.xp).isEqualTo(15) // 10 + 5  = 15

    }



    private fun submitGetWeeklyLeaderboardHistory(): WeeklyLeaderboardResponse =
        TestRestClient
            .getOk(
                ApiPaths.LEADERBOARD.BASE,
                user1.id,
                WeeklyLeaderboardResponse::class.java
            )

}