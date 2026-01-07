package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID
import kotlin.test.Test

class CreateCourseIT : AbstractIntegrationTest() {

    @Test
    fun createCourse_createsCourse_returnsNewCourses () {

        val newCourseName = "C++"
        val requestHash = UUID.randomUUID()

        val req = CreateCourseRequest(newCourseName, requestHash)

        val res = submitPostCreateCourse(req)

        assertThat(res).isNotNull()
        assertThat(res.size).isEqualTo(3)

        val created = res.single { it.title == newCourseName }

        assertThat(created).isNotNull()
        assertThat(created.title).isEqualTo(newCourseName)
    }

    private fun submitPostCreateCourse(req: CreateCourseRequest): List<CourseResponse> =
        TestRestClient
            .postOk(
                ApiPaths.SNAPSHOTS.COURSE,
                user1.id!!,
                req,
                Array<CourseResponse>::class.java
            )
            .toList()

}