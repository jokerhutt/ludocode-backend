package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.api.dto.response.DailyXpHistoryResponse
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.domain.entity.UserXp
import com.ludocode.ludocodebackend.progress.domain.entity.XpTransaction
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.app.service.UserXpService
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.support.util.LessonSubmissionTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

class XpServiceIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var userXpService: UserXpService

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun findOrCreateXp_newUser_startsAtZero_noTransaction() {
        val userId = user1.id!!

        val result = userXpService.findOrCreateXp(userId)

        assertThat(result.xp).isEqualTo(0)

        val transactions = xpTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        assertThat(transactions).isEmpty()
    }

    @Test
    fun apply_addsXp_createsTransaction() {
        val userId = user1.id!!
        userXpRepository.save(UserXp(userId = userId, xp = 0))

        val result = userXpService.apply(userId, 10)

        assertThat(result.xp).isEqualTo(10)

        val transactions = xpTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        assertThat(transactions).hasSize(1)
        assertThat(transactions[0].amount).isEqualTo(10)
        assertThat(transactions[0].balanceAfter).isEqualTo(10)
    }

    @Test
    fun apply_multipleRewards_balanceAccumulates() {
        val userId = user1.id!!
        userXpRepository.save(UserXp(userId = userId, xp = 0))

        val res1 = userXpService.apply(userId, 5)
        val res2 = userXpService.apply(userId, 10)

        assertThat(res1.xp).isEqualTo(5)
        assertThat(res2.xp).isEqualTo(15)

        val transactions = xpTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .sortedBy { it.balanceAfter }
        assertThat(transactions).hasSize(2)
        assertThat(transactions[0].balanceAfter).isEqualTo(5)
        assertThat(transactions[1].balanceAfter).isEqualTo(15)
    }

    @Test
    fun applyLessonXp_perfectAccuracy_grants10Xp() {
        val userId = user1.id!!
        userXpRepository.save(UserXp(userId = userId, xp = 0))

        val (result, gained) = userXpService.applyLessonXp(userId, BigDecimal.ONE)

        assertThat(gained).isEqualTo(10)
        assertThat(result.xp).isEqualTo(10)
    }

    @Test
    fun applyLessonXp_imperfectAccuracy_grants5Xp() {
        val userId = user1.id!!
        userXpRepository.save(UserXp(userId = userId, xp = 0))

        val (result, gained) = userXpService.applyLessonXp(userId, BigDecimal("0.75"))

        assertThat(gained).isEqualTo(5)
        assertThat(result.xp).isEqualTo(5)
    }

    @Test
    fun submitLesson_createsXpTransaction() {
        val userId = user1.id!!
        userCoinsRepository.save(UserCoins(userId, 0))
        userStreakRepository.save(UserStreak(userId = userId))

        courseProgressRepository.save(
            CourseProgress(
                id = CourseProgressId(userId, pythonId),
                currentModuleId = pyMod1Id,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock)
            )
        )

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonSnap = pythonSnap.modules[0].lessons[0]

        val response = LessonSubmissionTestUtil.completeLesson(userId, lessonSnap, pythonId)
        val content = response.content!!

        assertThat(content.newXp.xp).isGreaterThan(0)
        assertThat(content.xpGained).isIn(5, 10)

        val transactions = xpTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        assertThat(transactions).hasSize(1)
        assertThat(transactions[0].amount).isEqualTo(content.xpGained)
        assertThat(transactions[0].balanceAfter).isEqualTo(content.newXp.xp)
        assertThat(transactions[0].userId).isEqualTo(userId)
    }

    @Test
    fun submitTwoLessons_createsTwoTransactions_balanceMatchesFinal() {
        val userId = user1.id!!
        userCoinsRepository.save(UserCoins(userId, 0))
        userStreakRepository.save(UserStreak(userId = userId))

        courseProgressRepository.save(
            CourseProgress(
                id = CourseProgressId(userId, pythonId),
                currentModuleId = pyMod1Id,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock)
            )
        )

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lesson1 = pythonSnap.modules[0].lessons[0]
        val lesson2 = pythonSnap.modules[0].lessons[1]

        LessonSubmissionTestUtil.completeLesson(userId, lesson1, pythonId)
        val response2 = LessonSubmissionTestUtil.completeLesson(userId, lesson2, pythonId)
        val finalXp = response2.content!!.newXp.xp

        val transactions = xpTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .sortedBy { it.balanceAfter }
        assertThat(transactions).hasSize(2)
        assertThat(transactions[1].balanceAfter).isEqualTo(finalXp)
    }

    @Test
    fun getXpHistory_noTransactions_returnsZerosForAllDays() {
        val userId = user1.id!!

        val history = submitGetXpHistory(userId, 7)

        assertThat(history).hasSize(7)
        assertThat(history.sumOf { it.xp }).isEqualTo(0)
    }

    @Test
    fun getXpHistory_withTransactions_returnsCorrectDailySums() {
        val userId = user1.id!!
        userXpRepository.save(UserXp(userId = userId, xp = 25))

        val now = OffsetDateTime.now(clock)

        xpTransactionRepository.save(
            XpTransaction(userId = userId, amount = 10, balanceAfter = 10, createdAt = now.minusHours(2))
        )
        xpTransactionRepository.save(
            XpTransaction(userId = userId, amount = 5, balanceAfter = 15, createdAt = now.minusHours(1))
        )
        xpTransactionRepository.save(
            XpTransaction(userId = userId, amount = 10, balanceAfter = 25, createdAt = now.minusDays(1))
        )

        val history = submitGetXpHistory(userId, 7)

        assertThat(history).hasSize(7)

        val today = now.toLocalDate()
        val todayEntry = history.first { it.date == today }
        assertThat(todayEntry.xp).isEqualTo(15)

        val yesterdayEntry = history.first { it.date == today.minusDays(1) }
        assertThat(yesterdayEntry.xp).isEqualTo(10)

        val otherDays = history.filter { it.date != today && it.date != today.minusDays(1) }
        assertThat(otherDays).allMatch { it.xp == 0 }
    }

    @Test
    fun getXpHistory_defaultDaysIsSeven() {
        val userId = user1.id!!

        val history = submitGetXpHistory(userId, null)

        assertThat(history).hasSize(7)
    }

    private fun submitGetXpHistory(userId: UUID, days: Int?): List<DailyXpHistoryResponse> =
        TestRestClient
            .getOk(
                ApiPaths.PROGRESS.XP.history(days),
                userId,
                Array<DailyXpHistoryResponse>::class.java
            )
            .toList()
}



