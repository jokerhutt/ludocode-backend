package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.Test

class ExercisesIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun getExercises_returnsExercises() {

        val lesson1: Lesson = py1Lessons[0]
        val lesson2: Lesson = py1Lessons[0]

        val response: List<ExerciseResponse> = submitGetExercisesByLessonId(lesson1.id!!)
        val response2: List<ExerciseResponse> = submitGetExercisesByLessonId(lesson2.id!!)

        assertThat(response).isNotEmpty()
        assertThat(response2).isNotEmpty()

        for (res: ExerciseResponse in response) {

            assertThat(res.correctOptions.size).isGreaterThanOrEqualTo(1)
            assertThat(res.lessonId).isEqualTo(lesson1.id)
            assertThat(res.title).isNotNull()
            assertThat(res.prompt).isNotNull()
            assertThat(res.exerciseType).isEqualTo(ExerciseType.CLOZE)
        }

        for (res: ExerciseResponse in response2) {
            assertThat(res.correctOptions.size).isGreaterThanOrEqualTo(1)
            assertThat(res.exerciseType).isNotEqualTo(ExerciseType.CLOZE)
            assertThat(res.lessonId).isEqualTo(lesson2.id)
        }

    }

    private fun submitGetExercisesByLessonId(lessonId: UUID): List<ExerciseResponse> =
        given()
            .`when`()
            .get("${PathConstants.CATALOG}/exercises/$lessonId")
            .then()
            .statusCode(200)
            .extract()
            .`as`(Array<ExerciseResponse>::class.java)
            .toList()

}