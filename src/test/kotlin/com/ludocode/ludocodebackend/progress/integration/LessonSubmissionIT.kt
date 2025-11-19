package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotBuilderService
import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROGRESS_COMPLETION
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SUBMIT_COMPLETION
import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.OffsetDateTime

import java.util.UUID
import kotlin.test.Test

class LessonSubmissionIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var snapshotBuilderService: SnapshotBuilderService

    @Test
    fun submitLesson_endOfModule_returnsFirstLessonOfNextModule() {

        val userCoins = userCoinsRepository.save(UserCoins(user1.id!!, 0))
        userStreakRepository.save(UserStreak(userId = user1.id!!))

        val pythonSnap = snapshotBuilderService.buildCourseSnapshot(pythonId)

        assertThat(pythonSnap).isNotNull()

        val currentCourse = pythonId
        val currentLesson = py1L4
        val nextLesson = py2L1


        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, currentCourse), currentLessonId = currentLesson, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentLessonId = sw1L2, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
        ))

        val exercises : List<ExerciseSnap> = pythonSnap.modules[0].lessons[3].exercises


        val sub1 = ExerciseSubmissionRequest(
            exerciseId = exercises[0].id,
            version = 1,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[0].id!!,
                    isCorrect = true,
                    answer = exercises[0].correctOptions.map { it -> AttemptToken(it.exerciseOptionId, it.content) },
                )
            )
        )

        val sub2 = ExerciseSubmissionRequest(
            exerciseId = exercises[1].id!!,
            version = 1,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id!!,
                    isCorrect = false,
                    answer = exercises[1].distractors.map { it -> AttemptToken(it.exerciseOptionId, it.content) },
                ),
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id!!,
                    isCorrect = true,
                    answer = exercises[1].correctOptions.map { it -> AttemptToken(it.exerciseOptionId, it.content) },
                )
            )
        )

        val submissions: List<ExerciseSubmissionRequest> = listOf(sub1, sub2)
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), currentLesson, submissions = submissions)

        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        assertThat(response).isNotNull()

        val content : LessonCompletionResponse = response.content!!

        assertThat(response.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newCoins.coins).isGreaterThan(0)
        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(nextLesson)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2Id)
        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
        assertThat(content.accuracy).isGreaterThan(BigDecimal("0.00"))
        assertThat(content.accuracy).isLessThan(BigDecimal("1.00"))
        assertThat(content.updatedCompletedLesson.id).isEqualTo(currentLesson)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)

        assertThat(content.newStreak.lastMet).isNotNull()
        assertThat(content.newStreak.current).isEqualTo(1)

    }

    @Test
    fun submitLesson_endOfCourse_returnsEndOfCourse() {

        val userCoins = userCoinsRepository.save(UserCoins(user1.id!!, 0))


        val currentCourse = pythonId
        val currentLesson = py2L2

        courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, currentCourse), currentLessonId = currentLesson, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentLessonId = sw1L3, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
        ))

        val pythonSnap = snapshotBuilderService.buildCourseSnapshot(pythonId)

        val exercises : List<ExerciseSnap> = pythonSnap.modules[1].lessons[1].exercises


        val sub1 = ExerciseSubmissionRequest(
            exerciseId = exercises[0].id!!,
            version = 1,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[0].id!!,
                    isCorrect = true,
                    answer = exercises[0].correctOptions.map { it -> AttemptToken(it.exerciseOptionId, it.content) },
                )
            )
        )


        val submissions: List<ExerciseSubmissionRequest> = listOf(sub1)
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), currentLesson, submissions = submissions)

        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        assertThat(response).isNotNull()

        val content = response.content!!
        assertThat(response.status).isEqualTo(LessonCompletionStatus.COURSE_COMPLETE)
        assertThat(content.newCourseProgress.courseId).isEqualTo(currentCourse)
        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(currentLesson)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2Id)
        assertThat(content.newCoins.coins).isGreaterThan(0)
        assertThat(content.accuracy).isGreaterThan(BigDecimal("0.00"))
        assertThat(content.accuracy).isEqualTo(BigDecimal("1.00"))
        assertThat(content.updatedCompletedLesson.id).isEqualTo(currentLesson)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)


    }


    @Test
    fun submitLesson_returnsNextLesson() {
        // user stats
        userCoinsRepository.save(UserCoins(user1.id!!, 0))

        // progress: start at Python L1, expect next = L2
        val currentCourse = pythonId
        val currentLesson = py1L1
        val nextLesson = py1L2

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(id = CourseProgressId(user1.id!!, currentCourse), currentLessonId = currentLesson, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
                CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentLessonId = sw1L3, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
            )
        )

        // snapshot for tokens
        val pythonSnap = snapshotBuilderService.buildCourseSnapshot(pythonId)
        val lesson0 = pythonSnap.modules[0].lessons[0]
        val exercises: List<ExerciseSnap> = lesson0.exercises
        require(exercises.size >= 2) { "Lesson 1 must have at least two exercises for this test." }


        // Ex2: wrong then right to avoid 1.00 accuracy
        val sub1 = ExerciseSubmissionRequest(
            exerciseId = exercises[0].id,
            version = 1,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[0].id,
                    isCorrect = false,
                    answer = exercises[0].distractors.map { it -> AttemptToken(it.exerciseOptionId, it.content) },
                ),
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id,
                    isCorrect = true,
                    answer = exercises[0].correctOptions.map { it -> AttemptToken(it.exerciseOptionId, it.content) },
                )
            )
        )

        // Ex1: correct in one attempt
        val sub2 = ExerciseSubmissionRequest(
            exerciseId = exercises[1].id,
            version = 1,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id,
                    isCorrect = true,
                    answer = exercises[1].correctOptions.map { it -> AttemptToken(it.exerciseOptionId, it.content) },
                )
            )
        )



        val submissions = listOf(sub1, sub2)
        val req = LessonSubmissionRequest(UUID.randomUUID(), currentLesson, submissions)

        val packet = submitPostForLessonSubmission(user1.id!!, req)
        val content = packet.content!!

        // assertions
        assertThat(packet.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newCoins.coins).isGreaterThan(0)
        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
        assertThat(content.newCourseProgress.courseId).isEqualTo(currentCourse)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod1Id)
        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(nextLesson)

        assertThat(content.accuracy).isGreaterThan(BigDecimal("0.00"))
        assertThat(content.accuracy).isLessThan(BigDecimal("1.00"))

        assertThat(content.updatedCompletedLesson.id).isEqualTo(currentLesson)
        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()
    }

    private fun submitPostForLessonSubmission(userId: UUID, submission: LessonSubmissionRequest): LessonCompletionPacket =
        given()
            .header("X-Test-User-Id", userId.toString())
            .contentType(io.restassured.http.ContentType.JSON)
            .body(submission)
            .`when`()
            .post("$PROGRESS_COMPLETION$SUBMIT_COMPLETION")
            .then()
            .statusCode(200)
            .extract()
            .`as`(LessonCompletionPacket::class.java)


}


