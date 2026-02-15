package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.util.CourseProgressTestUtil
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.support.util.LessonSubmissionTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepetitionInfo
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.OffsetDateTime

import java.util.UUID
import kotlin.test.Test

class LessonSubmissionIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun submitLesson_success_resubmitSameLessonLater_success() {

        val userCoins = userCoinsRepository.save(UserCoins(user1.id!!, 0))
        userStreakRepository.save(UserStreak(userId = user1.id!!))

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)

        assertThat(pythonSnap).isNotNull()

        val currentCourse = pythonId
        val currentLesson = py1L4

        val currentModule = pyMod1Id

        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, currentCourse), currentModuleId = currentModule, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentModuleId = swMod1Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
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
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), currentLesson, submissions = submissions, courseId = currentCourse)

        val res1 = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        val secondLessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), currentLesson,  courseId = currentCourse, submissions)
        val res2 = submitPostForLessonSubmission(user1.id!!, secondLessonCompletionRequest)
        val content : LessonCompletionResponse = res2.content!!

        assertThat(res2.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newCoins.coins).isGreaterThan(0)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod1Id)
        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
        assertThat(content.accuracy).isGreaterThan(BigDecimal(0))
        assertThat(content.accuracy).isLessThan(BigDecimal(1))
        assertThat(content.updatedCompletedLesson.id).isEqualTo(currentLesson)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)

    }

    @Test
    fun submitLesson_duplicate_returnsDuplicateError() {

        val userCoins = userCoinsRepository.save(UserCoins(user1.id!!, 0))
        userStreakRepository.save(UserStreak(userId = user1.id!!))

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)

        assertThat(pythonSnap).isNotNull()

        val currentCourse = pythonId
        val currentLesson = py1L4
        val nextLesson = py2L1


        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, currentCourse), currentModuleId = pyMod1Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentModuleId = swMod1Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
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
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), currentLesson, courseId = pythonId, submissions = submissions)

        val res1 = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        val secondLessonCompletionRequest = LessonSubmissionRequest(lessonCompletionRequest.submissionId, currentLesson, courseId = pythonId, submissions)
        val res2 = submitPostForLessonSubmission(user1.id!!, secondLessonCompletionRequest)
        assertThat(res2.status).isEqualTo(LessonCompletionStatus.DUPLICATE)
        assertThat(res2.content).isNull()

    }

    @Test
    fun submitLesson_endOfModule_returnsSameModule() {

        val userCoins = userCoinsRepository.save(UserCoins(user1.id!!, 0))
        userStreakRepository.save(UserStreak(userId = user1.id!!))

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)

        assertThat(pythonSnap).isNotNull()

        val currentCourse = pythonId
        val currentLesson = py1L4
        val nextLesson = py2L1


        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, currentCourse), currentModuleId = pyMod1Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentModuleId = swMod1Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
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
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), currentLesson, courseId = currentCourse, submissions = submissions)

        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        assertThat(response).isNotNull()

        val content : LessonCompletionResponse = response.content!!

        assertThat(response.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newCoins.coins).isGreaterThan(0)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod1Id)
        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
        assertThat(content.accuracy).isGreaterThan(BigDecimal(0))
        assertThat(content.accuracy).isLessThan(BigDecimal(1))
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
            CourseProgress(id = CourseProgressId(user1.id!!, currentCourse), currentModuleId = pyMod2Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentModuleId = swMod1Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
        ))

        lessonCompletionRepository.saveAll(CourseProgressTestUtil.pythonProgress(user1.id, currentCourse, pythonLessons))

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)

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
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), currentLesson, courseId = currentCourse, submissions = submissions)

        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        assertThat(response).isNotNull()

        val content = response.content!!
        assertThat(response.status).isEqualTo(LessonCompletionStatus.COURSE_COMPLETE)
        assertThat(content.newCourseProgress.courseId).isEqualTo(currentCourse)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2Id)
        assertThat(content.newCoins.coins).isGreaterThan(0)
        assertThat(content.accuracy).isGreaterThan(BigDecimal(0))
        assertThat(content.accuracy).isEqualByComparingTo("1")
        assertThat(content.updatedCompletedLesson.id).isEqualTo(currentLesson)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)


    }

    @Test
    fun submitInfoLesson_returnsNextLessonAndCompleteWithFullAccuracy() {
        val userId = user1.id!!
        userCoinsRepository.save(UserCoins(userId, 0))

        val currentCourse = swiftId
        val currentLesson = sw1L1
        val nextLesson = sw1L2

        courseProgressRepository.save(
            CourseProgress(
                id = CourseProgressId(userId, currentCourse),
                currentModuleId = swMod1Id,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock)
            )
        )

        val swiftSnap = testSnapshotService.buildCourseSnapshot(swiftId)
        val infoLesson = swiftSnap.modules[0].lessons[0]
        val exercises = infoLesson.exercises
        assertThat(exercises).isNotEmpty

        val submissions = exercises.map { ex ->
            ExerciseSubmissionRequest(
                exerciseId = ex.id,
                version = 1,
                attempts = listOf(
                    ExerciseAttemptRequest(
                        exerciseId = ex.id,
                        isCorrect = true,
                        answer = listOf(
                            AttemptToken(
                                id = UUID.randomUUID(),
                                value = "I"
                            )
                        )
                    )
                )
            )
        }

        val req = LessonSubmissionRequest(
            submissionId = UUID.randomUUID(),
            lessonId = currentLesson,
            courseId = swiftId,
            submissions = submissions
        )

        val packet = submitPostForLessonSubmission(userId, req)
        val content = packet.content!!

        assertThat(packet.status).isEqualTo(LessonCompletionStatus.OK)

        assertThat(content.accuracy).isEqualByComparingTo("1")

        assertThat(content.updatedCompletedLesson.id).isEqualTo(currentLesson)
        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()

        assertThat(content.newCourseProgress.moduleId).isEqualTo(swMod1Id)

        assertThat(content.newCoins.coins).isGreaterThanOrEqualTo(0)
    }


    @Test
    fun submitLesson_returnsNextLesson() {
        userCoinsRepository.save(UserCoins(user1.id!!, 0))

        val currentCourse = pythonId
        val currentLesson = py1L1
        val nextLesson = py1L2

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(id = CourseProgressId(user1.id!!, currentCourse), currentModuleId = pyMod1Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
                CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentModuleId = swMod1Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
            )
        )

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lesson0 = pythonSnap.modules[0].lessons[0]
        val exercises: List<ExerciseSnap> = lesson0.exercises
        require(exercises.size >= 2) { "Lesson 1 must have at least two exercises for this test." }


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
        val req = LessonSubmissionRequest(UUID.randomUUID(), currentLesson, courseId = pythonId, submissions)

        val packet = submitPostForLessonSubmission(user1.id!!, req)
        val content = packet.content!!

        assertThat(packet.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newCoins.coins).isGreaterThan(0)
        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
        assertThat(content.newCourseProgress.courseId).isEqualTo(currentCourse)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod1Id)

        assertThat(content.accuracy).isGreaterThan(BigDecimal("0.00"))
        assertThat(content.accuracy).isLessThan(BigDecimal("1.00"))

        assertThat(content.updatedCompletedLesson.id).isEqualTo(currentLesson)
        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()



    }

    private fun submitPostForLessonSubmission(userId: UUID, submission: LessonSubmissionRequest): LessonCompletionPacket =
        TestRestClient.postOk(ApiPaths.PROGRESS.COMPLETION.BASE, userId, submission, LessonCompletionPacket::class.java)

    @RepeatedTest(20, name = "Random Lesson Submission - Run {currentRepetition}/{totalRepetitions}")
    fun submitRandomLesson_calculatesCorrectScoreAndAccuracy(repetitionInfo: RepetitionInfo) {
        val seed = repetitionInfo.currentRepetition.toLong()
        val random = java.util.Random(seed)

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

        val courseSnap = testSnapshotService.buildCourseSnapshot(courseId)
        assertThat(courseSnap.modules).isNotEmpty()

        val randomModuleIndex = random.nextInt(courseSnap.modules.size)
        val randomModule = courseSnap.modules[randomModuleIndex]
        assertThat(randomModule.lessons).isNotEmpty()

        val randomLessonIndex = random.nextInt(randomModule.lessons.size)
        val randomLesson = randomModule.lessons[randomLessonIndex]

        // Create random exercise submissions with varying success patterns
        val submissions = randomLesson.exercises.map { exercise ->
            LessonSubmissionTestUtil.createRandomExerciseSubmission(exercise, random)
        }

        // Calculate expected accuracy
        val totalAttempts = submissions.sumOf { it.attempts.size }
        val correctAttempts = submissions.sumOf { sub -> sub.attempts.count { it.isCorrect } }
        val expectedAccuracy = if (totalAttempts > 0) {
            BigDecimal(correctAttempts).divide(BigDecimal(totalAttempts), 2, BigDecimal.ROUND_HALF_UP)
        } else {
            BigDecimal.ONE
        }

        val request = LessonSubmissionRequest(
            submissionId = UUID.randomUUID(),
            lessonId = randomLesson.id,
            courseId = courseId,
            submissions = submissions
        )

        val response = submitPostForLessonSubmission(user1.id!!, request)

        assertThat(response).isNotNull()
        assertThat(response.content).isNotNull()

        val content = response.content!!

        assertThat(content.accuracy).isEqualByComparingTo(expectedAccuracy)

        assertThat(content.newCoins.coins).isGreaterThanOrEqualTo(0)

        assertThat(content.updatedCompletedLesson.id).isEqualTo(randomLesson.id)
        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()

        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
        assertThat(content.newCourseProgress.courseId).isEqualTo(courseId)
    }

    @RepeatedTest(15, name = "Random Perfect Score Lesson - Run {currentRepetition}/{totalRepetitions}")
    fun submitRandomLesson_perfectScore_hasFullAccuracy(repetitionInfo: RepetitionInfo) {
        val seed = repetitionInfo.currentRepetition.toLong()
        val random = java.util.Random(seed)

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

        val courseSnap = testSnapshotService.buildCourseSnapshot(courseId)
        val randomModuleIndex = random.nextInt(courseSnap.modules.size)
        val randomModule = courseSnap.modules[randomModuleIndex]
        val randomLessonIndex = random.nextInt(randomModule.lessons.size)
        val randomLesson = randomModule.lessons[randomLessonIndex]

        val submissions = randomLesson.exercises.map { exercise ->
            LessonSubmissionTestUtil.createPerfectExerciseSubmission(exercise)
        }

        val request = LessonSubmissionRequest(
            submissionId = UUID.randomUUID(),
            lessonId = randomLesson.id,
            courseId = courseId,
            submissions = submissions
        )

        val response = submitPostForLessonSubmission(user1.id!!, request)
        val content = response.content!!

        assertThat(content.accuracy).isEqualByComparingTo(BigDecimal.ONE)

        assertThat(content.newCoins.coins).isGreaterThan(0)

        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()
    }

    @RepeatedTest(15, name = "Random Imperfect Score Lesson - Run {currentRepetition}/{totalRepetitions}")
    fun submitRandomLesson_imperfectScore_hasPartialAccuracy(repetitionInfo: RepetitionInfo) {
        val seed = repetitionInfo.currentRepetition.toLong()
        val random = java.util.Random(seed)

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

        val courseSnap = testSnapshotService.buildCourseSnapshot(courseId)
        val randomModuleIndex = random.nextInt(courseSnap.modules.size)
        val randomModule = courseSnap.modules[randomModuleIndex]
        val randomLessonIndex = random.nextInt(randomModule.lessons.size)
        val randomLesson = randomModule.lessons[randomLessonIndex]

        val submissions = randomLesson.exercises.map { exercise ->
            LessonSubmissionTestUtil.createImperfectExerciseSubmission(exercise, random)
        }

        val request = LessonSubmissionRequest(
            submissionId = UUID.randomUUID(),
            lessonId = randomLesson.id,
            courseId = courseId,
            submissions = submissions
        )

        val response = submitPostForLessonSubmission(user1.id!!, request)
        val content = response.content!!

        assertThat(content.accuracy).isLessThan(BigDecimal.ONE)
        assertThat(content.accuracy).isGreaterThan(BigDecimal.ZERO)

        assertThat(content.newCoins.coins).isGreaterThanOrEqualTo(0)

        assertThat(content.updatedCompletedLesson.isCompleted).isTrue()
    }


}
