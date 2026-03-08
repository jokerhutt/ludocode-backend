package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.request.CourseStatusRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
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

class CourseVisibilityIT : AbstractIntegrationTest(){

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun courseNotVisible_doesNotShow() {
        val newCourseName = "Python New"
        val requestHash = UUID.randomUUID()

        val initialCourseCount = submitGetAllCourses().size

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
    fun makeAllCoursesInvisible_throwsError () {

        val allCourses = submitGetAllCourses()
        assertThat(allCourses.size).isEqualTo(2)

        val firstCourseThatShouldSucceedId = allCourses[0].id
        val secondCourseThatShouldFailId = allCourses[1].id

        submitPutToggleCourseVisibility(CourseStatusRequest(false), firstCourseThatShouldSucceedId)
        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        assertPutToggleVisibilityError(CourseStatusRequest(false), secondCourseThatShouldFailId, ErrorCode.NO_ALL_COURSES_INVISIBLE)

    }


    @Test
    fun updateCourseVisibility_updatesVisiblity_courseIncludedInFetches () {

        val newCourseName = "Python New"
        val requestHash = UUID.randomUUID()

        val initialCourseCount = submitGetAllCourses().size

        val setupReq = CreateCourseRequest(newCourseName, requestHash, "New python course that is awesome", CourseType.COURSE, "Star", pythonLanguage.id)
        submitPostCreateCourse(setupReq)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val newCourseSnap = buildCourseSnapshotByTitle(newCourseName)

        val setupAllCoursesRes = submitGetAllCourses()
        assertThat(setupAllCoursesRes.size).isEqualTo(initialCourseCount)
        assertThat(setupAllCoursesRes)
            .noneMatch { it.id == newCourseSnap.courseId }

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val req = CourseStatusRequest(value = true)

        submitPutToggleCourseVisibility(req, newCourseSnap.courseId)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val refreshedCoursesRes = submitGetAllCourses()

        assertThat(refreshedCoursesRes.size).isEqualTo(initialCourseCount + 1)
        assertThat(refreshedCoursesRes)
            .anyMatch { it.id == newCourseSnap.courseId }

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

    private fun assertPutToggleVisibilityError(req: CourseStatusRequest, courseId: UUID, errorCode: ErrorCode) : ValidatableResponse? {
        return TestRestClient.assertError("PUT", ApiPaths.SNAPSHOTS.byCourseAdminVisibility(courseId), user1.id!!, req, errorCode)
    }

    private fun submitPutToggleCourseVisibility(req: CourseStatusRequest, courseId: UUID): List<CourseResponse> =
        TestRestClient
            .putOk(
                ApiPaths.SNAPSHOTS.byCourseAdminVisibility(courseId),
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