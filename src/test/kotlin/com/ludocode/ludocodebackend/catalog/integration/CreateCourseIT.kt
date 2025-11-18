package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.commons.constants.PathConstants.CREATE_COURSE
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SNAPSHOT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SUBMIT_COURSE_SNAPSHOT
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
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
        given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body(req)
            .`when`()
            .post("$SNAPSHOT$CREATE_COURSE")
            .then()
            .statusCode(200)
            .extract()
            .`as`(object : io.restassured.common.mapper.TypeRef<List<CourseResponse>>() {})

}