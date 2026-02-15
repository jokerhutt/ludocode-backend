package com.ludocode.ludocodebackend.progress.integration
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressStats
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.user.domain.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

class CourseProgressIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @BeforeEach
    fun seed () {

    }

    @Test
    fun resetCourseProgress_returnsCleared () {

        val user: User = user1

        val course1Id = pythonId
        val course1CurrentModule = pyMod2Id

        val course2Id = swiftId
        val course2CurrentModule = swMod1Id

        courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user.id!!, course1Id), currentModuleId = course1CurrentModule, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user.id!!, course2Id), currentModuleId = course2CurrentModule, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
        ))

        val courseToReset = course1Id

        val response = submitResetCourseProgress(user.id!!, courseToReset)

        assertThat(response).isNotNull()
        assertThat(response.courseId).isEqualTo(courseToReset)
        assertThat(response.moduleId).isEqualTo(pyMod1Id)

    }

    @Test
    fun getCourseProgressList_returnsExisting () {

        val user: User = user1

        val course1Id = pythonId
        val course1CurrentModule = pyMod2Id

        val course2Id = swiftId
        val course2CurrentModule = swMod1Id

        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user.id!!, course1Id), currentModuleId = course1CurrentModule, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user.id!!, course2Id), currentModuleId = course2CurrentModule, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
        ))

        val enrolledIds : List<UUID> = progressList.map { progress -> progress.id.courseId!! }

        val response = submitGetCourseProgressList(user.id!!, enrolledIds)

        assertThat(response.size).isEqualTo(2)
        for (res: CourseProgressResponse in response) {
            assertThat(res.userId).isEqualTo(user.id)
            assertThat(res.moduleId).isNotNull()
        }

        val byCourse = response.associateBy { it.courseId }

        assertThat(byCourse[course1Id]!!.moduleId).isEqualTo(course1CurrentModule)

        assertThat(byCourse[course2Id]!!.moduleId).isEqualTo(course2CurrentModule)
    }

    @Test
    fun completeLessonInOtherModule_changesCurrentModuleId () {

        val user: User = user1
        val courseId = pythonId
        val currentModule = pyMod2Id

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val progress = courseProgressRepository.save(CourseProgress(id = CourseProgressId(user.id!!, courseId), currentModuleId = currentModule, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)))

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

        val lessonCompletionRequest = LessonSubmissionRequest(UUID.randomUUID(), pythonSnap.modules[0].lessons[1].id, courseId = pythonId, submissions = submissions)
        submitPostForLessonSubmission(user1.id!!, lessonCompletionRequest)


        val newsub1 = pythonSnap.modules[1].lessons[0]

        val newsubEx = LessonSubmissionRequest(
            UUID.randomUUID(),
            newsub1.id,
            pythonId, submissions = listOf(
                ExerciseSubmissionRequest(
                    exerciseId = newsub1.exercises[0].id,
                    version = 1,
                    attempts = listOf(
                        ExerciseAttemptRequest(
                           exerciseId = newsub1.exercises[0].id,
                            true,
                            listOf(AttemptToken( newsub1.exercises[0].correctOptions[0].exerciseOptionId, newsub1.exercises[0].correctOptions[0].content))
                        )
                    )
                )
            )
        )
        val res = submitPostForLessonSubmission(user1.id!!, newsubEx)


        assertThat(res.content!!.newCourseProgress.moduleId).isEqualTo(pyMod2Id)





    }

    @Test
    fun getCourseProgressList_NoCourses_returnsEmpty () {
        val user: User = user1
        courseProgressRepository.deleteAll()
        val response = submitGetCourseProgressList(user.id!!, listOf())
        assertThat(response).isEmpty()

    }

    private fun submitGetCourseProgressList(
        userId: UUID,
        courseIds: List<UUID>
    ): List<CourseProgressResponse> =
        TestRestClient
            .getOk(
                ApiPaths.PROGRESS.COURSES.BASE,
                userId,
                Array<CourseProgressResponse>::class.java,
                mapOf("courseIds" to courseIds)
            )
            .toList()

    private fun submitGetCourseStatsList(
        userId: UUID,
        courseIds: List<UUID>
    ): List<CourseProgressStats> =
        TestRestClient
            .getOk(
                 "${ApiPaths.PROGRESS.COURSES.BASE}${ApiPaths.PROGRESS.COURSES.STATS}",
                userId,
                Array<CourseProgressStats>::class.java,
                mapOf("courseIds" to courseIds)
            )
            .toList()

    private fun submitResetCourseProgress(userId: UUID, courseId: UUID):CourseProgressResponse =
        TestRestClient.postOk(ApiPaths.PROGRESS.COURSES.reset(courseId), userId, null, CourseProgressResponse::class.java)
    private fun submitPostForLessonSubmission(userId: UUID, submission: LessonSubmissionRequest): LessonCompletionPacket =
        TestRestClient.postOk(ApiPaths.PROGRESS.COMPLETION.BASE, userId, submission, LessonCompletionPacket::class.java)



}