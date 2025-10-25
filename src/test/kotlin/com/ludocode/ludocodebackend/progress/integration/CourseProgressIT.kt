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
    fun resetCourseProgress_returnsCleared () {

        val user: User = user1

        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user.id!!, pythonCourse.id!!), currentLessonId = pyModule2Lessons[2].id),
            CourseProgress(id = CourseProgressId(user.id!!, swiftCourse.id!!), currentLessonId = swiftModuleLessons[2].id)
        ))

        val courseToReset = progressList[0]

        val response = submitResetCourseProgress(user.id!!, courseToReset.id!!.courseId!!)

        assertThat(response).isNotNull()
        assertThat(response.courseId).isEqualTo(courseToReset.id.courseId)
        assertThat(response.moduleId).isEqualTo(pyModule1.id)
        assertThat(response.currentLessonId).isEqualTo(pyModule1Lessons[0].id)

    }

    @Test
    fun getCourseProgressList_returnsExisting () {

        val user: User = user1

        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user.id!!, pythonCourse.id!!), currentLessonId = pyModule2Lessons[2].id),
            CourseProgress(id = CourseProgressId(user.id!!, swiftCourse.id!!), currentLessonId = swiftModuleLessons[2].id)
        ))

        val enrolledIds : List<UUID> = progressList.map { progress -> progress.id.courseId!! }

        val response = submitGetCourseProgressList(user.id!!, enrolledIds)

        assertThat(response.size).isEqualTo(2)
        for (res: CourseProgressResponse in response) {
            assertThat(res.userId).isEqualTo(user.id)
            assertThat(res.currentLessonId).isNotNull()
        }

        val byCourse = response.associateBy { it.courseId }

        assertThat(byCourse[pythonCourse.id]!!.currentLessonId).isEqualTo(pyModule2Lessons[2].id)
        assertThat(byCourse[pythonCourse.id]!!.moduleId).isEqualTo(pyModule2Lessons[2].moduleId)
        assertThat(byCourse[pythonCourse.id]!!.moduleId).isEqualTo(pyModule2.id)

        assertThat(byCourse[swiftCourse.id]!!.currentLessonId).isEqualTo(swiftModuleLessons[2].id)
        assertThat(byCourse[swiftCourse.id]!!.moduleId).isEqualTo(swiftModule1.id)
        assertThat((byCourse[swiftCourse.id]!!.moduleId)).isEqualTo(swiftModuleLessons[2].moduleId)

    }

    @Test
    fun getCourseProgressList_NoCourses_returnsEmpty () {

        val user: User = user1
        courseProgressRepository.deleteAll()

        val response = submitGetCourseProgressList(user.id!!, listOf())

        assertThat(response).isEmpty()

    }


    private fun submitGetCourseProgressList(userId: UUID, courseIds: List<UUID>): List<CourseProgressResponse> =
        given()
            .header("X-Test-User-Id", userId.toString())
            .queryParam("courseIds", courseIds)
            .`when`()
            .get("$PROGRESS_COURSE/ids")
            .then()
            .statusCode(200)
            .extract()
            .`as`(Array<CourseProgressResponse>::class.java)
            .toList()

    private fun submitResetCourseProgress(userId: UUID, courseId: UUID):CourseProgressResponse =
        given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .post("$PROGRESS_COURSE/$courseId/reset")
            .then()
            .statusCode(200)
            .extract()
            .`as`(CourseProgressResponse::class.java)




}