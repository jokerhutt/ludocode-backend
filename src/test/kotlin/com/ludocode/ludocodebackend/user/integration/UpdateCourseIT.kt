package com.ludocode.ludocodebackend.user.integration

import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROGRESS_COURSE
import com.ludocode.ludocodebackend.commons.constants.PathConstants.UPDATE_COURSE
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UpdatedCourseResponse
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

class UpdateCourseIT : AbstractIntegrationTest() {


    @BeforeEach
    fun seed () {

    }

    @Test
    fun updateCourse_returnExistingCourseProgress () {

        val user = user1
        val now = OffsetDateTime.now(clock)

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(
                    id = CourseProgressId(user.id!!, pythonId),
                    currentLessonId = py2L3,
                    updatedAt = now.minusDays(1),
                    createdAt = now.minusDays(2),
                ),
                CourseProgress(
                    id = CourseProgressId(user.id!!, swiftId),
                    currentLessonId = sw1L3,
                    updatedAt = now,
                    createdAt = now.minusDays(1)
                )
            )
        )


        val response = submitPostUpdateCurrentCourse(userId = user.id!!, newCourseId = swiftId)

        assertThat(response).isNotNull()

        val courseProgressResponse = response.courseProgress

        assertThat(courseProgressResponse.courseId).isEqualTo(swiftId)
        assertThat(courseProgressResponse.userId).isEqualTo(user.id)
        assertThat(courseProgressResponse.currentLessonId).isEqualTo(sw1L3)

    }

    @Test
    fun updateCourse_createsNewCourseProgress() {

        courseProgressRepository.deleteAll()
        val user = user1
        val newCourse = pythonId
        val firstLessonOfCourse = py1L1

        val response = submitPostUpdateCurrentCourse(user.id!!, newCourse)

        assertThat(response).isNotNull()
        assertThat(response.courseProgress.courseId).isEqualTo(newCourse)
        assertThat(response.courseProgress.currentLessonId).isEqualTo(firstLessonOfCourse)

    }


    private fun submitPostUpdateCurrentCourse(userId: UUID, newCourseId: UUID): CourseProgressResponseWithEnrolled =
        given()
            .header("X-Test-User-Id", userId.toString())
            .contentType(io.restassured.http.ContentType.JSON)
            .body(ChangeCourseRequest(newCourseId))
            .`when`()
            .post("$PROGRESS_COURSE$UPDATE_COURSE")
            .then()
            .statusCode(200)
            .extract()
            .`as`(CourseProgressResponseWithEnrolled::class.java)


}