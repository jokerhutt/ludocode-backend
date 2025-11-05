package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatModule
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.user.domain.entity.User
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CourseTreeIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun getCourseTree_returnsCourseTree () {

        val user: User = user1
        val course: Course = python

        val response: FlatCourseTreeResponse = submitGetCourseTree(course.id!!, user.id!!)

        // == ASSERT == //
        assertThat(response.modules).isNotEmpty()

        for (module: FlatModule in response.modules) {
            assertThat(module.lessons).isNotEmpty()
        }

    }

    private fun submitGetCourseTree (courseId: UUID, userId: UUID): FlatCourseTreeResponse {
        return given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .get("${PathConstants.CATALOG}/courses/$courseId/tree")
            .then()
            .statusCode(200)
            .extract()
            .`as`(FlatCourseTreeResponse::class.java)
    }


}