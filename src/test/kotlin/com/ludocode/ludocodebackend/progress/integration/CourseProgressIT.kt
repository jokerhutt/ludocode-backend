package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROGRESS_COURSE
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.user.domain.entity.User
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.Test

class CourseProgressIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun getCourseProgressList_returnsExisting () {

        val user: User = user1

        courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user.id!!, pythonCourse.id!!), currentLessonId = pyModule2Lessons[2].id),
            CourseProgress(id = CourseProgressId(user.id!!, swiftCourse.id!!), currentLessonId = swiftModuleLessons[2].id)
        ))

        val response = submitGetCourseProgressList(user.id!!)

        assertThat(response.size).isEqualTo(2)
        for (res: CourseProgressResponse in response) {
            assertThat(res.userId).isEqualTo(user.id)
            assertThat(res.currentLessonId).isNotNull()
        }

        val courseProgress1 = response[0]
        val courseProgress2 = response[1]

        assertThat(courseProgress1.currentLessonId).isEqualTo(pyModule2Lessons[2].id)
        assertThat(courseProgress1.moduleId).isEqualTo(pyModule2.id)
        assertThat(courseProgress1.moduleId).isEqualTo(pyModule2Lessons[2].moduleId)
        assertThat(courseProgress2.currentLessonId).isEqualTo(swiftModuleLessons[2].id)
        assertThat(courseProgress2.moduleId).isEqualTo(swiftModule1.id)
        assertThat(courseProgress2.moduleId).isEqualTo(swiftModuleLessons[2].moduleId)

    }

    @Test
    fun getCourseProgressList_NoCourses_returnsEmpty () {

        val user: User = user1
        courseProgressRepository.deleteAll()

        val response = submitGetCourseProgressList(user.id!!)

        assertThat(response).isEmpty()

    }


    private fun submitGetCourseProgressList(userId: UUID): List<CourseProgressResponse> =
        given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .get("$PROGRESS_COURSE/list")
            .then()
            .statusCode(200)
            .extract()
            .`as`(Array<CourseProgressResponse>::class.java)
            .toList()




}