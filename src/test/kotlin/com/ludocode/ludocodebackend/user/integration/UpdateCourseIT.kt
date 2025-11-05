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
        val now = OffsetDateTime.now()

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(
                    id = CourseProgressId(user.id!!, python.id!!),
                    currentLessonId = py2Lessons[2].id,
                    updatedAt = now.minusDays(1)
                ),
                CourseProgress(
                    id = CourseProgressId(user.id!!, swift.id!!),
                    currentLessonId = sw1Lessons[2].id,
                    updatedAt = now
                )
            )
        )


        val response = submitPostUpdateCurrentCourse(userId = user.id!!, newCourseId = swift.id!!)

        assertThat(response).isNotNull()

        val courseProgressResponse = response.courseProgress

        assertThat(courseProgressResponse.courseId).isEqualTo(swift.id)
        assertThat(courseProgressResponse.userId).isEqualTo(user.id)
        assertThat(courseProgressResponse.currentLessonId).isEqualTo(sw1Lessons[2].id)

    }

    @Test
    fun updateCourse_createsNewCourseProgress() {

        courseProgressRepository.deleteAll()
        val user = user1
        val newCourse = python
        val firstLessonOfCourse = py1Lessons[0]

        val response = submitPostUpdateCurrentCourse(user.id!!, newCourse.id!!)

        assertThat(response).isNotNull()
        assertThat(response.courseProgress.courseId).isEqualTo(newCourse.id)
        assertThat(response.courseProgress.currentLessonId).isEqualTo(firstLessonOfCourse.id)

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