package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.Test

class DeleteCourseIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed() {

    }

    @Test
    fun deleteCourse_deletesCourse_returnsRemaining() {

        val courses = submitGetAllCourses()
        assertThat(courses.size).isGreaterThan(0)
        val courseToDelete = courses[0]

        val res = submitDeleteCourse(courseToDelete.id)

        assertThat(res.size).isEqualTo(courses.size - 1)
        assertThat(res)
            .noneMatch { it.id == courseToDelete.id }
    }

    @Test
    fun deleteCourse_deletesCourse_thenTriesToDeleteLast_throwsError() {

        val courses = submitGetAllCourses()
        assertThat(courses.size).isEqualTo(2)

        submitDeleteCourse(courses[1].id)

        courseRepository.flush()
        moduleRepository.flush()
        moduleLessonsRepository.flush()

        val courseToDeleteId = courses[0].id
        assertErrorOnDelete(courseToDeleteId, ErrorCode.NO_LAST_COURSE_DELETE)



    }

    private fun submitDeleteCourse(courseId: UUID): List<CourseResponse> =
        TestRestClient
            .deleteOk(
                ApiPaths.SNAPSHOTS.byCourseAdmin(courseId),
                user1.id!!,
                Array<CourseResponse>::class.java
            )
            .toList()

    private fun assertErrorOnDelete(courseId: UUID, errorCode: ErrorCode): ValidatableResponse? {
        return TestRestClient.assertError("DELETE", ApiPaths.SNAPSHOTS.byCourseAdmin(courseId), user1.id!!, null, errorCode)
    }

    private fun submitGetAllCourses(): List<CourseResponse> =
        TestRestClient
            .getOk(
                "${ApiPaths.CATALOG.BASE}${ApiPaths.CATALOG.COURSES}",
                user1.id!!,
                Array<CourseResponse>::class.java
            )
            .toList()


}