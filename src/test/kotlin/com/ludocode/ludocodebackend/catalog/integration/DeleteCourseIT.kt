package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
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

class DeleteCourseIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun deleteDraftCourse_deletesCourse_returnsRemaining() {
        val newCourseName = "Draft To Delete"
        val requestHash = UUID.randomUUID()

        val req = CreateCourseRequest(newCourseName, requestHash, "A draft course", CourseType.COURSE, "Star", pythonLanguage)
        val coursesAfterCreate = submitPostCreateCourse(req)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val newCourseSnap = buildCourseSnapshotByTitle(newCourseName)
        val courseToDeleteId = newCourseSnap.courseId

        assertThat(coursesAfterCreate).anyMatch { it.id == courseToDeleteId }

        val res = submitDeleteCourse(courseToDeleteId)

        assertThat(res)
            .noneMatch { it.id == courseToDeleteId }
    }

    @Test
    fun deletePublishedCourse_throwsError() {
        val publishedCourses = submitGetAllCourses()
        assertThat(publishedCourses.size).isGreaterThan(0)

        val publishedCourseId = publishedCourses[0].id

        assertErrorOnDelete(publishedCourseId)
    }

    private fun submitPostCreateCourse(req: CreateCourseRequest): List<CourseResponse> =
        TestRestClient
            .postOk(
                "${ApiPaths.SNAPSHOTS.ADMIN_BASE}${ApiPaths.SNAPSHOTS.COURSE}",
                user1.id,
                req,
                Array<CourseResponse>::class.java
            )
            .toList()

    private fun submitDeleteCourse(courseId: UUID): List<CourseResponse> =
        TestRestClient
            .deleteOk(
                ApiPaths.SNAPSHOTS.byCourseAdmin(courseId),
                user1.id,
                Array<CourseResponse>::class.java
            )
            .toList()

    private fun assertErrorOnDelete(courseId: UUID): ValidatableResponse {
        return TestRestClient.assertError("DELETE", ApiPaths.SNAPSHOTS.byCourseAdmin(courseId), user1.id, null, ErrorCode.NO_DELETE_NON_DRAFT_COURSE)
    }

    fun buildCourseSnapshotByTitle(title: String): CourseSnap {
        val course = courseRepository.findByTitle(title)
            ?: throw IllegalStateException("Course not found")

        return testSnapshotService.buildCourseSnapshot(course.id)
    }

    private fun submitGetAllCourses(): List<CourseResponse> =
        TestRestClient
            .getOk(
                "${ApiPaths.CATALOG.BASE}${ApiPaths.CATALOG.COURSES}",
                user1.id,
                Array<CourseResponse>::class.java
            )
            .toList()

}