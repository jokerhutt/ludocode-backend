package com.ludocode.ludocodebackend.user.integration

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.commons.constants.PathConstants.UPDATE_COURSE
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UpdatedCourseResponse
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.Test

class UpdateCourseIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun updateCourse_returnExistingCourseProgress () {

        val user = user1
        courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user.id!!, pythonCourse.id!!), currentLessonId = pyModule2Lessons[2].id),
            CourseProgress(id = CourseProgressId(user.id!!, swiftCourse.id!!), currentLessonId = swiftModuleLessons[2].id)
        ))

        user.currentCourse = pythonCourse.id!!
        userRepository.save(user)

        val response = submitPatchUpdateCurrentCourse(userId = user.id!!, newCourseId = swiftCourse.id!!)

        assertThat(response).isNotNull()

        val userResponse = response.user
        val courseProgressResponse = response.courseProgess

        assertThat(userResponse.id).isEqualTo(user.id)
        assertThat(userResponse.currentCourse).isEqualTo(swiftCourse.id)
        assertThat(courseProgressResponse.courseId).isEqualTo(swiftCourse.id)
        assertThat(courseProgressResponse.userId).isEqualTo(user.id)
        assertThat(courseProgressResponse.currentLessonId).isEqualTo(swiftModuleLessons[2].id)

    }

    private fun submitPatchUpdateCurrentCourse(userId: UUID, newCourseId: UUID): UpdatedCourseResponse =
        given()
            .header("X-Test-User-Id", userId.toString())
            .contentType(io.restassured.http.ContentType.JSON)
            .body(ChangeCourseRequest(newCourseId))
            .`when`()
            .post("$USERS$UPDATE_COURSE")   // <-- POST, not PATCH
            .then()
            .statusCode(200)
            .extract()
            .`as`(UpdatedCourseResponse::class.java)


}