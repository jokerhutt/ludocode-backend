package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROGRESS_COMPLETION
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SUBMIT_COMPLETION
import com.ludocode.ludocodebackend.commons.constants.PathConstants.UPDATE_COURSE
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UpdatedCourseResponse
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat

import java.util.UUID
import kotlin.test.Test

class LessonSubmissionIT : AbstractIntegrationTest() {

    @Test
    fun submitLesson_endOfModule_returnsFirstLessonOfNextModule() {

        val userStats = userStatsRepository.save(UserStats(user1.id!!, 0, 0))



        val lesson1: Lesson = pyModule1Lessons[3]
        val nextLesson: Lesson = pyModule2Lessons[0]


        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, pythonCourse.id!!), currentLessonId = lesson1.id),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftCourse.id!!), currentLessonId = swiftModuleLessons[2].id)
        ))

        val exercises = exerciseRepository.saveAll(listOf(
            Exercise(title = "Complete the expression", prompt = "let sum = ___ + 4", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),
            Exercise(title = "Create a variable with a value of 'House'", prompt = "const ___ = ___", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),

            ))

        val exerciseOptions = exerciseOptionRepository.saveAll(listOf(
            ExerciseOption(content = "4", answerOrder = 1, exerciseId = exercises[0].id),
            ExerciseOption(content = "4", answerOrder = null, exerciseId = exercises[0].id),

            ExerciseOption(content = "house", answerOrder = 1, exerciseId = exercises[1].id),
            ExerciseOption(content = "'house'", answerOrder = 2, exerciseId = exercises[1].id),

            ))

        val sub1 = ExerciseSubmissionRequest(
            exerciseId = exercises[0].id!!,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[0].id!!,
                    isCorrect = true,
                    answer = listOf("let", "sum", "=", "4"),
                )
            )
        )

// For exercise 1 → user tried once WRONG, then tried again and got it RIGHT
        val sub2 = ExerciseSubmissionRequest(
            exerciseId = exercises[1].id!!,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id!!,
                    isCorrect = false,
                    answer = listOf("const", "x", "=", "house"), // wrong
                ),
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id!!,
                    isCorrect = true,
                    answer = listOf("const", "x", "=", "'house'"),
                )
            )
        )

        val submissions: List<ExerciseSubmissionRequest> = listOf(sub1, sub2)
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), pyModule1.id!!, lesson1.id!!, submissions = submissions)

        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        assertThat(response).isNotNull()

        val content : LessonCompletionResponse = response.content!!

        assertThat(response.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newStats.coins).isGreaterThan(0)
        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(nextLesson.id)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonCourse.id)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyModule2.id)
        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)

        assertThat(content.updatedCompletedLesson.id).isEqualTo(lesson1.id)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)

    }

    @Test
    fun submitLesson_endOfCourse_returnsEndOfCourse() {

        val userStats = userStatsRepository.save(UserStats(user1.id!!, 0, 0))

        val lesson1: Lesson = pyModule2Lessons[3]

        courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, pythonCourse.id!!), currentLessonId = lesson1.id),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftCourse.id!!), currentLessonId = swiftModuleLessons[2].id)
        ))

        val exercises = exerciseRepository.saveAll(listOf(
            Exercise(title = "Complete the expression", prompt = "let sum = ___ + 4", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),
            Exercise(title = "Create a variable with a value of 'House'", prompt = "const ___ = ___", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),
            ))

        val exerciseOptions = exerciseOptionRepository.saveAll(listOf(
            ExerciseOption(content = "4", answerOrder = 1, exerciseId = exercises[0].id),
            ExerciseOption(content = "4", answerOrder = null, exerciseId = exercises[0].id),

            ExerciseOption(content = "house", answerOrder = 1, exerciseId = exercises[1].id),
            ExerciseOption(content = "'house'", answerOrder = 2, exerciseId = exercises[1].id),

            ))

        val sub1 = ExerciseSubmissionRequest(
            exerciseId = exercises[0].id!!,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[0].id!!,
                    isCorrect = true,
                    answer = listOf("let", "sum", "=", "4"),
                )
            )
        )

// For exercise 1 → user tried once WRONG, then tried again and got it RIGHT
        val sub2 = ExerciseSubmissionRequest(
            exerciseId = exercises[1].id!!,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id!!,
                    isCorrect = false,
                    answer = listOf("const", "x", "=", "house"), // wrong
                ),
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id!!,
                    isCorrect = true,
                    answer = listOf("const", "x", "=", "'house'"),
                )
            )
        )

        val submissions: List<ExerciseSubmissionRequest> = listOf(sub1, sub2)
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), pyModule1.id!!, lesson1.id!!, submissions = submissions)

        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        assertThat(response).isNotNull()

        val content = response.content!!
        assertThat(response.status).isEqualTo(LessonCompletionStatus.COURSE_COMPLETE)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonCourse.id)
        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(lesson1.id)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyModule2.id)
        assertThat(content.newStats.coins).isGreaterThan(0)

        assertThat(content.updatedCompletedLesson.id).isEqualTo(lesson1.id)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)


    }


    @Test
    fun submitLesson_returnsNextLesson() {

            val userStats = userStatsRepository.save(UserStats(user1.id!!, 0, 0))

            val progressList = courseProgressRepository.saveAll(listOf(
                CourseProgress(id = CourseProgressId(user1.id!!, pythonCourse.id!!), currentLessonId = pyModule2Lessons[2].id),
                CourseProgress(id = CourseProgressId(user1.id!!, swiftCourse.id!!), currentLessonId = swiftModuleLessons[2].id)
            ))

            val lesson1: Lesson = pyModule1Lessons[0]
            val nextLesson: Lesson = pyModule1Lessons[1]

            val exercises = exerciseRepository.saveAll(listOf(
                Exercise(title = "Complete the expression", prompt = "let sum = ___ + 4", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),
                Exercise(title = "Create a variable with a value of 'House'", prompt = "const ___ = ___", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),

            ))

            val exerciseOptions = exerciseOptionRepository.saveAll(listOf(
                ExerciseOption(content = "4", answerOrder = 1, exerciseId = exercises[0].id),
                ExerciseOption(content = "4", answerOrder = null, exerciseId = exercises[0].id),

                ExerciseOption(content = "house", answerOrder = 1, exerciseId = exercises[1].id),
                ExerciseOption(content = "'house'", answerOrder = 2, exerciseId = exercises[1].id),

            ))

        val sub1 = ExerciseSubmissionRequest(
            exerciseId = exercises[0].id!!,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[0].id!!,
                    isCorrect = true,
                    answer = listOf("let", "sum", "=", "4"),
                )
            )
        )

// For exercise 1 → user tried once WRONG, then tried again and got it RIGHT
        val sub2 = ExerciseSubmissionRequest(
            exerciseId = exercises[1].id!!,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id!!,
                    isCorrect = false,
                    answer = listOf("const", "x", "=", "house"), // wrong
                ),
                ExerciseAttemptRequest(
                    exerciseId = exercises[1].id!!,
                    isCorrect = true,
                    answer = listOf("const", "x", "=", "'house'"),
                )
            )
        )

        val submissions: List<ExerciseSubmissionRequest> = listOf(sub1, sub2)
        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), pyModule1.id!!, lesson1.id!!, submissions = submissions)

        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)

        assertThat(response).isNotNull()

        val content : LessonCompletionResponse = response.content!!

        assertThat(response.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newStats.coins).isGreaterThan(0)
        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(nextLesson.id)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonCourse.id)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyModule1.id)
        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)

        assertThat(content.updatedCompletedLesson.id).isEqualTo(lesson1.id)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)



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