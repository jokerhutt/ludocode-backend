package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.progress.api.dto.internal.PointsDelta
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.CoinTransactionType
import com.ludocode.ludocodebackend.progress.app.service.UserCoinsService
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.support.util.LessonSubmissionTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.util.*

class CoinsServiceIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var userCoinsService: UserCoinsService

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun apply_addsCoins_createsTransaction() {
        val userId = user1.id!!
        userCoinsRepository.save(UserCoins(userId, 0))

        val result = userCoinsService.apply(
            PointsDelta(
                userId = userId,
                pointsDelta = 10,
                transactionType = CoinTransactionType.LESSON_REWARD,
                referenceId = UUID.randomUUID()
            )
        )

        assertThat(result.coins).isEqualTo(10)

        val transactions = coinTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        assertThat(transactions).hasSize(1)
        assertThat(transactions[0].amount).isEqualTo(10)
        assertThat(transactions[0].balanceAfter).isEqualTo(10)
        assertThat(transactions[0].transactionType).isEqualTo(CoinTransactionType.LESSON_REWARD)
    }

    @Test
    fun apply_multipleRewards_balanceAccumulates() {
        val userId = user1.id!!
        userCoinsRepository.save(UserCoins(userId, 0))

        val res1 = userCoinsService.apply(
            PointsDelta(userId = userId, pointsDelta = 15, transactionType = CoinTransactionType.LESSON_REWARD)
        )
        val res2 = userCoinsService.apply(
            PointsDelta(userId = userId, pointsDelta = 25, transactionType = CoinTransactionType.BONUS)
        )

        assertThat(res1.coins).isEqualTo(15)
        assertThat(res2.coins).isEqualTo(40)

        val transactions = coinTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .sortedBy { it.balanceAfter }
        assertThat(transactions).hasSize(2)

        val first = transactions[0]
        assertThat(first.balanceAfter).isEqualTo(15)
        assertThat(first.transactionType).isEqualTo(CoinTransactionType.LESSON_REWARD)

        val latest = transactions[1]
        assertThat(latest.balanceAfter).isEqualTo(40)
        assertThat(latest.transactionType).isEqualTo(CoinTransactionType.BONUS)
    }

    @Test
    fun removeCoins_subtractsFromBalance_createsTransaction() {
        val userId = user1.id!!
        userCoinsRepository.save(UserCoins(userId, 50))

        val result = userCoinsService.removeCoins(
            userId = userId,
            amount = 20,
            transactionType = CoinTransactionType.STREAK_FREEZE_PURCHASE
        )

        assertThat(result.coins).isEqualTo(30)

        val transactions = coinTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        assertThat(transactions).hasSize(1)
        assertThat(transactions[0].amount).isEqualTo(-20)
        assertThat(transactions[0].balanceAfter).isEqualTo(30)
        assertThat(transactions[0].transactionType).isEqualTo(CoinTransactionType.STREAK_FREEZE_PURCHASE)
    }

    @Test
    fun removeCoins_withReferenceId_storesReferenceId() {
        val userId = user1.id!!
        val referenceId = UUID.randomUUID()
        userCoinsRepository.save(UserCoins(userId, 100))

        userCoinsService.removeCoins(
            userId = userId,
            amount = 30,
            transactionType = CoinTransactionType.STREAK_FREEZE_PURCHASE,
            referenceId = referenceId
        )

        val transactions = coinTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        assertThat(transactions).hasSize(1)
        assertThat(transactions[0].referenceId).isEqualTo(referenceId)
    }

    @Test
    fun findOrCreateCoins_newUser_startsAtZero_noTransaction() {
        val userId = user1.id!!

        val result = userCoinsService.findOrCreateCoins(userId)

        assertThat(result.coins).isEqualTo(0)

        val transactions = coinTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        assertThat(transactions).isEmpty()
    }

    @Test
    fun submitLesson_createsLessonRewardTransaction() {
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

        assertThat(content.newCoins.coins).isGreaterThan(0)

        val transactions = coinTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        assertThat(transactions).hasSize(1)

        val tx = transactions[0]
        assertThat(tx.transactionType).isEqualTo(CoinTransactionType.LESSON_REWARD)
        assertThat(tx.amount).isGreaterThan(0)
        assertThat(tx.balanceAfter).isEqualTo(content.newCoins.coins)
        assertThat(tx.referenceId).isEqualTo(lessonSnap.id)
        assertThat(tx.userId).isEqualTo(userId)
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
        val finalCoins = response2.content!!.newCoins.coins

        val transactions = coinTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .sortedBy { it.balanceAfter }
        assertThat(transactions).hasSize(2)

        val firstTx = transactions[0]
        assertThat(firstTx.referenceId).isEqualTo(lesson1.id)
        assertThat(firstTx.balanceAfter).isLessThan(finalCoins)

        val latestTx = transactions[1]
        assertThat(latestTx.balanceAfter).isEqualTo(finalCoins)
        assertThat(latestTx.referenceId).isEqualTo(lesson2.id)
    }
}