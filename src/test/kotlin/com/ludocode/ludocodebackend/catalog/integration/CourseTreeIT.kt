package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseTreeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleNodeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
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

        // == ARRANGE == //
        val user: User = user1
        val course: Course = pythonCourse

        // == ACT == //
        val response: CourseTreeResponse = submitGetCourseTree(course.id!!, user.id!!)

        // == ASSERT == //
        assertThat(response.modules).isNotEmpty()

        for (moduleNode: ModuleNodeResponse in response.modules) {
            assertThat(moduleNode.module.title).isNotNull()
            assertThat(moduleNode.lessons).isNotEmpty()
        }

    }

    private fun submitGetCourseTree (courseId: UUID, userId: UUID): CourseTreeResponse {
        return given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .get("${PathConstants.CATALOG}/courses/$courseId/tree")
            .then()
            .statusCode(200)
            .extract()
            .`as`(CourseTreeResponse::class.java)
    }


}