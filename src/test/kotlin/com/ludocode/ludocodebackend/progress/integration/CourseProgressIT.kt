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

        val course1Id = pythonId
        val course1CurrentLesson = py2L2

        val course2Id = swiftId
        val course2CurrentLesson = sw1L3

        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user.id!!, course1Id), currentLessonId = course1CurrentLesson),
            CourseProgress(id = CourseProgressId(user.id!!, course2Id), currentLessonId = course2CurrentLesson)
        ))

        val courseToReset = course1Id

        val response = submitResetCourseProgress(user.id!!, courseToReset)

        assertThat(response).isNotNull()
        assertThat(response.courseId).isEqualTo(courseToReset)
        assertThat(response.moduleId).isEqualTo(pyMod1Id)
        assertThat(response.currentLessonId).isEqualTo(py1L1)

    }

    @Test
    fun getCourseProgressList_returnsExisting () {

        val user: User = user1

        val course1Id = pythonId
        val course1CurrentModule = pyMod2Id
        val course1CurrentLesson = py2L2

        val course2Id = swiftId
        val course2CurrentModule = swMod1Id
        val course2CurrentLesson = sw1L3

        val progressList = courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user.id!!, course1Id), currentLessonId = course1CurrentLesson),
            CourseProgress(id = CourseProgressId(user.id!!, course2Id), currentLessonId = course2CurrentLesson)
        ))

        val enrolledIds : List<UUID> = progressList.map { progress -> progress.id.courseId!! }

        val response = submitGetCourseProgressList(user.id!!, enrolledIds)

        assertThat(response.size).isEqualTo(2)
        for (res: CourseProgressResponse in response) {
            assertThat(res.userId).isEqualTo(user.id)
            assertThat(res.currentLessonId).isNotNull()
        }

        val byCourse = response.associateBy { it.courseId }

        assertThat(byCourse[course1Id]!!.currentLessonId).isEqualTo(course1CurrentLesson)
        assertThat(byCourse[course1Id]!!.moduleId).isEqualTo(course1CurrentModule)

        assertThat(byCourse[course2Id]!!.currentLessonId).isEqualTo(course2CurrentLesson)
        assertThat(byCourse[course2Id]!!.moduleId).isEqualTo(course2CurrentModule)

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
            .queryParam("courseIds", *courseIds.toTypedArray())
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