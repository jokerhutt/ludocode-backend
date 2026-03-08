package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.request.CourseStatusRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseStatus
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.CourseSnap
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import kotlin.test.Test

class CourseStatusIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun draftCourse_doesNotShowInCatalog() {
        val newCourseName = "Python New"
        val requestHash = UUID.randomUUID()

        val initialCourses = submitGetAllCourses()
        val initialCourseCount = initialCourses.size
        assertThat(initialCourses).allMatch { it.courseStatus == CourseStatus.PUBLISHED }

        val req = CreateCourseRequest(newCourseName, requestHash, "New python course that is awesome", CourseType.COURSE, "Star", pythonLanguage.id)
        submitPostCreateCourse(req)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val newCourseSnap = buildCourseSnapshotByTitle(newCourseName)

        val res = submitGetAllCourses()
        assertThat(res.size).isEqualTo(initialCourseCount)
        assertThat(res)
            .noneMatch { it.id == newCourseSnap.courseId }
    }

    @Test
    fun archiveAllPublishedCourses_throwsError() {
        val allCourses = submitGetAllCourses()
        assertThat(allCourses.size).isEqualTo(2)
        assertThat(allCourses).allMatch { it.courseStatus == CourseStatus.PUBLISHED }

        val firstCourseId = allCourses[0].id
        val secondCourseId = allCourses[1].id

        submitPutChangeCourseStatus(CourseStatusRequest(CourseStatus.ARCHIVED), firstCourseId)
        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        assertPutChangeCourseStatusError(CourseStatusRequest(CourseStatus.ARCHIVED), secondCourseId, ErrorCode.NO_ALL_COURSES_INVISIBLE)
    }

    @Test
    fun archiveDraftCourse_throwsError() {
        val newCourseName = "Python New"
        val requestHash = UUID.randomUUID()

        val req = CreateCourseRequest(newCourseName, requestHash, "New python course that is awesome", CourseType.COURSE, "Star", pythonLanguage.id)
        submitPostCreateCourse(req)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val newCourseSnap = buildCourseSnapshotByTitle(newCourseName)

        assertPutChangeCourseStatusError(CourseStatusRequest(CourseStatus.ARCHIVED), newCourseSnap.courseId, ErrorCode.NO_ARCHIVING_DRAFT_COURSE)
    }

    @Test
    fun undraftCourse_throwsError() {
        val newCourseName = "Python New"
        val requestHash = UUID.randomUUID()

        val req = CreateCourseRequest(newCourseName, requestHash, "New python course that is awesome", CourseType.COURSE, "Star", pythonLanguage.id)
        submitPostCreateCourse(req)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val setupCourseSnap = buildCourseSnapshotByTitle(newCourseName)

        submitPutChangeCourseStatus(CourseStatusRequest(CourseStatus.PUBLISHED), setupCourseSnap.courseId)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val newCourseSnap = buildCourseSnapshotByTitle(newCourseName)

        // Try to set an already-draft course to DRAFT
        assertPutChangeCourseStatusError(CourseStatusRequest(CourseStatus.DRAFT), newCourseSnap.courseId, ErrorCode.NO_UNDRAFTING_COURSE)
    }

    @Test
    fun publishDraftCourse_courseAppearsInCatalog() {
        val newCourseName = "Python New"
        val requestHash = UUID.randomUUID()

        val initialCourseCount = submitGetAllCourses().size

        val setupReq = CreateCourseRequest(newCourseName, requestHash, "New python course that is awesome", CourseType.COURSE, "Star", pythonLanguage.id)
        submitPostCreateCourse(setupReq)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val newCourseSnap = buildCourseSnapshotByTitle(newCourseName)

        // Draft course should not appear in catalog
        val coursesBeforePublish = submitGetAllCourses()
        assertThat(coursesBeforePublish.size).isEqualTo(initialCourseCount)
        assertThat(coursesBeforePublish)
            .noneMatch { it.id == newCourseSnap.courseId }

        // Publish the course
        submitPutChangeCourseStatus(CourseStatusRequest(CourseStatus.PUBLISHED), newCourseSnap.courseId)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        // Published course should now appear in catalog
        val coursesAfterPublish = submitGetAllCourses()
        assertThat(coursesAfterPublish.size).isEqualTo(initialCourseCount + 1)
        assertThat(coursesAfterPublish)
            .anyMatch { it.id == newCourseSnap.courseId && it.courseStatus == CourseStatus.PUBLISHED }
    }

    @Test
    fun archivePublishedCourse_courseStillAppearsInCatalogAsArchived() {
        val allCourses = submitGetAllCourses()
        assertThat(allCourses.size).isEqualTo(2)
        assertThat(allCourses).allMatch { it.courseStatus == CourseStatus.PUBLISHED }

        val courseToArchiveId = allCourses[0].id

        submitPutChangeCourseStatus(CourseStatusRequest(CourseStatus.ARCHIVED), courseToArchiveId)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val coursesAfterArchive = submitGetAllCourses()
        assertThat(coursesAfterArchive.size).isEqualTo(2)

        val archivedCourse = coursesAfterArchive.first { it.id == courseToArchiveId }
        assertThat(archivedCourse.courseStatus).isEqualTo(CourseStatus.ARCHIVED)

        val remainingPublished = coursesAfterArchive.filter { it.courseStatus == CourseStatus.PUBLISHED }
        assertThat(remainingPublished.size).isEqualTo(1)
    }

    fun buildCourseSnapshotByTitle(title: String): CourseSnap {
        val course = courseRepository.findByTitle(title)
            ?: throw IllegalStateException("Course not found")

        return testSnapshotService.buildCourseSnapshot(course.id)
    }

    private fun submitPostCreateCourse(req: CreateCourseRequest): List<CourseResponse> =
        TestRestClient
            .postOk(
                "${ApiPaths.SNAPSHOTS.ADMIN_BASE}${ApiPaths.SNAPSHOTS.COURSE}",
                user1.id!!,
                req,
                Array<CourseResponse>::class.java
            )
            .toList()

    private fun assertPutChangeCourseStatusError(req: CourseStatusRequest, courseId: UUID, errorCode: ErrorCode): ValidatableResponse {
        return TestRestClient.assertError("PUT", ApiPaths.SNAPSHOTS.byCourseAdminStatus(courseId), user1.id!!, req, errorCode)
    }

    private fun submitPutChangeCourseStatus(req: CourseStatusRequest, courseId: UUID): List<CourseResponse> =
        TestRestClient
            .putOk(
                ApiPaths.SNAPSHOTS.byCourseAdminStatus(courseId),
                user1.id!!,
                req,
                Array<CourseResponse>::class.java
            )
            .toList()

    private fun submitGetAllCourses(): List<CourseResponse> =
        TestRestClient
            .getOk(
                "${ApiPaths.CATALOG.BASE}${ApiPaths.CATALOG.COURSES}",
                user1.id!!,
                Array<CourseResponse>::class.java
            )
            .toList()
}

