package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
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

    private fun submitDeleteCourse(courseId: UUID): List<CourseResponse> =
        TestRestClient
            .deleteOk(
                ApiPaths.SNAPSHOTS.byCourseAdmin(courseId),
                user1.id!!,
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