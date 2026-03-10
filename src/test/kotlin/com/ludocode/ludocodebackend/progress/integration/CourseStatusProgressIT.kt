package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.catalog.api.dto.request.CourseStatusRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseStatus
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.support.util.CatalogChangeTestUtil
import com.ludocode.ludocodebackend.support.util.LessonSubmissionTestUtil
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.Test

class CourseStatusProgressIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun enrolledInCourse_courseArchived_canReEnrollViaCourseSwitch() {
        val enrollResponse = submitChangeCourse(user1.id!!, pythonId)
        assertThat(enrollResponse.courseProgress.courseId).isEqualTo(pythonId)
        assertThat(enrollResponse.enrolled).contains(pythonId)

        val switchResponse = submitChangeCourse(user1.id!!, swiftId)
        assertThat(switchResponse.courseProgress.courseId).isEqualTo(swiftId)
        assertThat(switchResponse.enrolled).contains(pythonId, swiftId)

        submitPutChangeCourseStatus(CourseStatusRequest(CourseStatus.ARCHIVED), pythonId)
        courseRepository.flush()

        val reEnrollResponse = submitChangeCourse(user1.id!!, pythonId)
        assertThat(reEnrollResponse.courseProgress.courseId).isEqualTo(pythonId)
        assertThat(reEnrollResponse.enrolled).contains(pythonId, swiftId)
    }

    @Test
    fun switchToArchivedCourse_notPreviouslyEnrolled_throwsError() {
        val enrollResponse = submitChangeCourse(user1.id!!, pythonId)
        assertThat(enrollResponse.courseProgress.courseId).isEqualTo(pythonId)

        submitPutChangeCourseStatus(CourseStatusRequest(CourseStatus.ARCHIVED), swiftId)
        courseRepository.flush()

        assertChangeCourseError(user1.id!!, swiftId, ErrorCode.INVALID_ENROLLMENT)
    }

    @Test
    fun enrolledInArchivedCourse_canGetProgressAndCompleteLesson() {
        userCoinsRepository.save(UserCoins(user1.id!!, 0))
        userStreakRepository.save(UserStreak(userId = user1.id!!))

        courseProgressRepository.save(
            CourseProgress(
                id = CourseProgressId(user1.id!!, pythonId),
                currentModuleId = pyMod1Id,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock)
            )
        )
        courseProgressRepository.flush()

        submitPutChangeCourseStatus(CourseStatusRequest(CourseStatus.ARCHIVED), pythonId)
        courseRepository.flush()

        val progressList = submitGetCourseProgressList(user1.id!!, listOf(pythonId))
        assertThat(progressList).hasSize(1)
        assertThat(progressList[0].courseId).isEqualTo(pythonId)
        assertThat(progressList[0].moduleId).isEqualTo(pyMod1Id)

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonSnap = pythonSnap.modules[0].lessons[0]

        val submissionResult = LessonSubmissionTestUtil.completeLesson(
            user1.id!!, lessonSnap, pythonId
        )

        assertThat(submissionResult.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(submissionResult.content).isNotNull()
        assertThat(submissionResult.content!!.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(submissionResult.content!!.updatedCompletedLesson.id).isEqualTo(lessonSnap.id)
        assertThat(submissionResult.content!!.updatedCompletedLesson.isCompleted).isTrue()
    }

    @Test
    fun curriculumChange_removesUsersCurrentModule_moduleIdResetsToFirstModuleOfCourse() {
        // User is enrolled in python and currently on the second module
        courseProgressRepository.save(
            CourseProgress(
                id = CourseProgressId(user1.id!!, pythonId),
                currentModuleId = pyMod2Id,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock)
            )
        )
        courseProgressRepository.flush()

        // Verify the user is currently on pyMod2Id before the change
        val progressBefore = submitGetCourseProgressList(user1.id!!, listOf(pythonId))
        assertThat(progressBefore).hasSize(1)
        assertThat(progressBefore[0].moduleId).isEqualTo(pyMod2Id)

        // Build a brand-new curriculum that replaces ALL existing modules with one new module
        val newModule = CatalogChangeTestUtil.createModule(
            "Replacement Module",
            "Replacement Lesson One",
            "Replacement Lesson Two"
        )
        val newCurriculum = CurriculumDraftSnapshot(modules = mutableListOf(newModule))
        val updatedCurriculum = submitPutUpdateCurriculum(newCurriculum, pythonId)

        // The first (and only) module in the returned curriculum is the new module
        val newFirstModuleId = updatedCurriculum.modules[0].id

        // After the curriculum change, the controller calls resetAllModuleIdsInCourse,
        // which should have reset the user's currentModuleId to the new first module
        val progressAfter = submitGetCourseProgressList(user1.id!!, listOf(pythonId))
        assertThat(progressAfter).hasSize(1)
        assertThat(progressAfter[0].courseId).isEqualTo(pythonId)
        assertThat(progressAfter[0].moduleId).isEqualTo(newFirstModuleId)
    }

    private fun submitPutUpdateCurriculum(req: CurriculumDraftSnapshot, courseId: UUID): CurriculumDraftSnapshot =
        TestRestClient.putOk(
            ApiPaths.SNAPSHOTS.byCourseCurriculumAdmin(courseId),
            user1.id!!,
            req,
            CurriculumDraftSnapshot::class.java
        )

    private fun submitChangeCourse(userId: UUID, courseId: UUID): CourseProgressResponseWithEnrolled =
        TestRestClient.putOk(
            "${ApiPaths.PROGRESS.COURSES.BASE}${ApiPaths.PROGRESS.COURSES.CURRENT}",
            userId,
            ChangeCourseRequest(courseId),
            CourseProgressResponseWithEnrolled::class.java
        )

    private fun assertChangeCourseError(userId: UUID, courseId: UUID, errorCode: ErrorCode) {
        TestRestClient.assertError(
            "PUT",
            "${ApiPaths.PROGRESS.COURSES.BASE}${ApiPaths.PROGRESS.COURSES.CURRENT}",
            userId,
            ChangeCourseRequest(courseId),
            errorCode
        )
    }

    private fun submitPutChangeCourseStatus(req: CourseStatusRequest, courseId: UUID): List<CourseResponse> =
        TestRestClient.putOk(
            ApiPaths.SNAPSHOTS.byCourseAdminStatus(courseId),
            user1.id!!,
            req,
            Array<CourseResponse>::class.java
        ).toList()

    private fun submitGetCourseProgressList(userId: UUID, courseIds: List<UUID>): List<CourseProgressResponse> =
        TestRestClient.getOk(
            ApiPaths.PROGRESS.COURSES.BASE,
            userId,
            Array<CourseProgressResponse>::class.java,
            mapOf("courseIds" to courseIds)
        ).toList()
}
