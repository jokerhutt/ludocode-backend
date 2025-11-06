package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.catalog.app.service.SnapshotService
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROGRESS_COMPLETION
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SUBMIT_COMPLETION
import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
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
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

import java.util.UUID
import kotlin.test.Test

class LessonSubmissionIT : AbstractIntegrationTest() {

//    @Test
//    fun submitLesson_endOfModule_returnsFirstLessonOfNextModule() {
//
//        val userStats = userStatsRepository.save(UserStats(user1.id!!, 0, 0))
//
//
//
//        val lesson1: Lesson = py1Lessons[3]
//        val nextLesson: Lesson = py2Lessons[0]
//
//
//        val progressList = courseProgressRepository.saveAll(listOf(
//            CourseProgress(id = CourseProgressId(user1.id!!, python.id!!), currentLessonId = lesson1.id),
//            CourseProgress(id = CourseProgressId(user1.id!!, swift.id!!), currentLessonId = sw1Lessons[2].id)
//        ))
//
//
//        val sub1 = ExerciseSubmissionRequest(
//            exerciseId = exercises[0].exerciseId.id!!,
//            version = exercises[0].exerciseId.version,
//            attempts = listOf(
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[0].exerciseId.id!!,
//                    isCorrect = true,
//                    answer = listOf("let", "sum", "=", "4"),
//                )
//            )
//        )
//
//        val sub2 = ExerciseSubmissionRequest(
//            exerciseId = exercises[1].exerciseId.id!!,
//            version = exercises[1].exerciseId.version,
//            attempts = listOf(
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[1].exerciseId.id!!,
//                    isCorrect = false,
//                    answer = listOf("const", "x", "=", "house"), // wrong
//                ),
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[1].exerciseId.id!!,
//                    isCorrect = true,
//                    answer = listOf("const", "x", "=", "'house'"),
//                )
//            )
//        )
//
//        val submissions: List<ExerciseSubmissionRequest> = listOf(sub1, sub2)
//        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), lesson1.id!!, submissions = submissions)
//
//        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)
//
//        assertThat(response).isNotNull()
//
//        val content : LessonCompletionResponse = response.content!!
//
//        assertThat(response.status).isEqualTo(LessonCompletionStatus.OK)
//        assertThat(content.newStats.coins).isGreaterThan(0)
//        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(nextLesson.id)
//        assertThat(content.newCourseProgress.courseId).isEqualTo(python.id)
//        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2.id)
//        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
//        assertThat(content.accuracy).isGreaterThan(BigDecimal("0.00"))
//        assertThat(content.accuracy).isLessThan(BigDecimal("1.00"))
//        assertThat(content.updatedCompletedLesson.id).isEqualTo(lesson1.id)
//        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)
//
//    }

//    @Test
//    fun submitLesson_endOfCourse_returnsEndOfCourse() {
//
//        val userStats = userStatsRepository.save(UserStats(user1.id!!, 0, 0))
//
//        val lesson1: Lesson = py2Lessons[3]
//
//        courseProgressRepository.saveAll(listOf(
//            CourseProgress(id = CourseProgressId(user1.id!!, python.id!!), currentLessonId = lesson1.id),
//            CourseProgress(id = CourseProgressId(user1.id!!, swift.id!!), currentLessonId = sw1Lessons[2].id)
//        ))
//
//
//        val sub1 = ExerciseSubmissionRequest(
//            exerciseId = exercises[0].exerciseId.id!!,
//            version = exercises[0].exerciseId.versionNumber,
//            attempts = listOf(
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[0].exerciseId.id!!,
//                    isCorrect = true,
//                    answer = listOf(AttemptToken(id = exerciseOptions[0].id, value = dbOptions[0].content))
//                )
//            )
//        )
//
//// For exercise 1 → user tried once WRONG, then tried again and got it RIGHT
//        val sub2 = ExerciseSubmissionRequest(
//            exerciseId = exercises[1].exerciseId.id!!,
//            version = exercises[1].exerciseId.versionNumber,
//            attempts = listOf(
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[1].exerciseId.id!!,
//                    isCorrect = false,
//                    answer = listOf(AttemptToken(id = exerciseOptions[3].id, value = dbOptions[2].content),
//                        AttemptToken(id = exerciseOptions[2].id, value = dbOptions[1].content)), // wrong
//                ),
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[1].exerciseId.id!!,
//                    isCorrect = true,
//                    answer = listOf(AttemptToken(id = exerciseOptions[2].id, value = dbOptions[1].content),
//                        AttemptToken(id = exerciseOptions[3].id, value = dbOptions[2].content))
//                )
//            )
//        )
//
//        val submissions: List<ExerciseSubmissionRequest> = listOf(sub1, sub2)
//        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), lesson1.id!!, submissions = submissions)
//
//        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)
//
//        assertThat(response).isNotNull()
//
//        val content = response.content!!
//        assertThat(response.status).isEqualTo(LessonCompletionStatus.COURSE_COMPLETE)
//        assertThat(content.newCourseProgress.courseId).isEqualTo(python.id)
//        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(lesson1.id)
//        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2.id)
//        assertThat(content.newStats.coins).isGreaterThan(0)
//        assertThat(content.accuracy).isGreaterThan(BigDecimal("0.00"))
//        assertThat(content.accuracy).isLessThan(BigDecimal("1.00"))
//        assertThat(content.updatedCompletedLesson.id).isEqualTo(lesson1.id)
//        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)
//
//
//    }


//    @Autowired
//    private lateinit var snapshotService: SnapshotService
//
//    @Test
//    fun submitLesson_returnsNextLesson() {
//
//            val userStats = userStatsRepository.save(UserStats(user1.id!!, 0, 0))
//
//            val pythonSnap = snapshotService.getCourseSnapshot(pythonId)
//
//            val progressList = courseProgressRepository.saveAll(listOf(
//                CourseProgress(id = CourseProgressId(user1.id!!, pythonId), currentLessonId = py1L1),
//                CourseProgress(id = CourseProgressId(user1.id!!, swiftId), currentLessonId = sw1L3)
//            ))
//
//            val lesson1 = py1L1
//            val nextLesson = py1L2
//
//            val exercises = pythonSnap.modules[0].lessons[0].exercises
//
//
//        val sub1 = ExerciseSubmissionRequest(
//            exerciseId = exercises[0].id!!,
//            version = 1,
//            attempts = listOf(
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[0].id!!,
//                    isCorrect = true,
//                    answer = listOf(exercises[0].correctOptions.map { it -> AttemptToken(id = ) })
//                )
//            )
//        )
//
//// For exercise 1 → user tried once WRONG, then tried again and got it RIGHT
//        val sub2 = ExerciseSubmissionRequest(
//            exerciseId = exercises[1].exerciseId.id!!,
//            version = exercises[1].exerciseId.versionNumber,
//            attempts = listOf(
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[1].exerciseId.id!!,
//                    isCorrect = false,
//                    answer = listOf(AttemptToken(id = exerciseOptions[3].id, value = dbOptions[2].content),
//                        AttemptToken(id = exerciseOptions[2].id, value = dbOptions[1].content)), // wrong
//                ),
//                ExerciseAttemptRequest(
//                    exerciseId = exercises[1].exerciseId.id!!,
//                    isCorrect = true,
//                    answer = listOf(AttemptToken(id = exerciseOptions[2].id, value = dbOptions[1].content),
//                        AttemptToken(id = exerciseOptions[3].id, value = dbOptions[2].content))
//                )
//            )
//        )
//
//        val submissions: List<ExerciseSubmissionRequest> = listOf(sub1, sub2)
//        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), lesson1.id!!, submissions = submissions)
//
//        val response = submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)
//
//        assertThat(response).isNotNull()
//
//        val content : LessonCompletionResponse = response.content!!
//
//        assertThat(response.status).isEqualTo(LessonCompletionStatus.OK)
//        assertThat(content.newStats.coins).isGreaterThan(0)
//        assertThat(content.newCourseProgress.currentLessonId).isEqualTo(nextLesson.id)
//        assertThat(content.newCourseProgress.courseId).isEqualTo(python.id)
//        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod1.id)
//        assertThat(content.newCourseProgress.userId).isEqualTo(user1.id)
//        assertThat(content.accuracy).isGreaterThan(BigDecimal("0.00"))
//        assertThat(content.accuracy).isLessThan(BigDecimal("1.00"))
//
//
//        assertThat(content.updatedCompletedLesson.id).isEqualTo(lesson1.id)
//        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)
//
//
//
//    }

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


