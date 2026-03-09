package com.ludocode.ludocodebackend.progress.integration

import kotlin.random.Random
import java.util.UUID

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.support.util.CourseProgressTestUtil
import com.ludocode.ludocodebackend.support.util.LessonSubmissionTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.OffsetDateTime

class LessonSubmissionIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun submitLesson_success_resubmitSameLessonLater_success() {

        userCoinsRepository.save(UserCoins(user1.id!!, 0))
        userStreakRepository.save(UserStreak(userId = user1.id!!))

        val currentCourse = pythonId

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(
                    id = CourseProgressId(user1.id!!, currentCourse),
                    currentModuleId = pyMod1Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                ),
                CourseProgress(
                    id = CourseProgressId(user1.id!!, swiftId),
                    currentModuleId = swMod1Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                )
            )
        )

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonSnap = pythonSnap.modules[0].lessons[3]

        LessonSubmissionTestUtil.completeLesson(user1.id!!, lessonSnap, currentCourse)
        lessonCompletionRepository.flush()

        val res2 = LessonSubmissionTestUtil.completeLesson(user1.id!!, lessonSnap, currentCourse)
        val content = res2.content!!

        assertThat(res2.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newCoins.coins).isGreaterThan(0)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod1Id)
        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
        assertThat(content.accuracy)
            .isBetween(BigDecimal.ZERO, BigDecimal.ONE)
        assertThat(content.updatedCompletedLesson.id).isEqualTo(lessonSnap.id)
        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()
    }

    @Test
    fun submitLesson_duplicate_returnsDuplicateError() {

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonSnap = pythonSnap.modules[0].lessons[3]

        val currentCourse = pythonId

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(
                    id = CourseProgressId(user1.id!!, currentCourse),
                    currentModuleId = pyMod1Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                ),
                CourseProgress(
                    id = CourseProgressId(user1.id!!, swiftId),
                    currentModuleId = swMod1Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                )
            )
        )

        userCoinsRepository.save(UserCoins(user1.id!!, 0))
        userStreakRepository.save(UserStreak(userId = user1.id!!))

        val exercises = LessonSubmissionTestUtil
            .buildExercises(lessonSnap, allCorrect = true)

        val submissionId = UUID.randomUUID()

        val firstRequest = LessonSubmissionRequest(
            submissionId = submissionId,
            lessonId = lessonSnap.id,
            courseId = pythonId,
            exercises = exercises
        )

        submitPostForLessonSubmission(user1.id!!, firstRequest)

        lessonCompletionRepository.flush()

        val duplicateRequest = LessonSubmissionRequest(
            submissionId = submissionId,
            lessonId = lessonSnap.id,
            courseId = pythonId,
            exercises = exercises
        )

        val res2 = submitPostForLessonSubmission(user1.id!!, duplicateRequest)

        assertThat(res2.status).isEqualTo(LessonCompletionStatus.DUPLICATE)
        assertThat(res2.content).isNull()
    }

    @Test
    fun submitLesson_endOfCourse_returnsEndOfCourse() {

        userCoinsRepository.save(UserCoins(user1.id!!, 0))

        val currentCourse = pythonId
        val lessonId = py2L2

        courseProgressRepository.save(
            CourseProgress(
                id = CourseProgressId(user1.id!!, currentCourse),
                currentModuleId = pyMod2Id,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock)
            )
        )

        lessonCompletionRepository.saveAll(
            CourseProgressTestUtil.pythonProgress(user1.id, currentCourse, pythonLessons)
        )

        lessonCompletionRepository.flush()

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonSnap = pythonSnap.modules[1].lessons[1]

        val response = LessonSubmissionTestUtil.completeLesson(user1.id!!, lessonSnap, currentCourse)
        lessonCompletionRepository.flush()
        val content = response.content!!

        assertThat(response.status).isEqualTo(LessonCompletionStatus.COURSE_COMPLETE)
        assertThat(content.newCourseProgress.courseId).isEqualTo(currentCourse)
//        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2Id)
        assertThat(content.accuracy).isEqualByComparingTo(BigDecimal.ONE)
        assertThat(content.updatedCompletedLesson.id).isEqualTo(lessonId)
        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()
    }

    @RepeatedTest(20)
    fun submitRandomLesson_calculatesAccuracy() {

        val random = Random(System.nanoTime())

        userCoinsRepository.save(UserCoins(user1.id!!, 0))

        val courseId = if (random.nextBoolean()) pythonId else swiftId
        val moduleId = if (courseId == pythonId) pyMod1Id else swMod1Id

        courseProgressRepository.save(
            CourseProgress(
                id = CourseProgressId(user1.id!!, courseId),
                currentModuleId = moduleId,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock)
            )
        )

        val snap = testSnapshotService.buildCourseSnapshot(courseId)
        val module = snap.modules.random(random)
        val lesson = module.lessons.random(random)

        val submissions =
            lesson.exercises.map {
                LessonSubmissionTestUtil.createRandomExerciseSubmission(it, random)
            }

        val req = LessonSubmissionRequest(
            submissionId = UUID.randomUUID(),
            lessonId = lesson.id,
            courseId = courseId,
            exercises = submissions
        )

        val response = submitPostForLessonSubmission(user1.id!!, req)
        val content = response.content!!

        assertThat(content.accuracy)
            .isBetween(BigDecimal.ZERO, BigDecimal.ONE)
        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()
    }

    private fun submitPostForLessonSubmission(
        userId: UUID,
        submission: LessonSubmissionRequest
    ): LessonCompletionPacket =
        TestRestClient.postOk(
            ApiPaths.PROGRESS.COMPLETION.BASE,
            userId,
            submission,
            LessonCompletionPacket::class.java
        )
}