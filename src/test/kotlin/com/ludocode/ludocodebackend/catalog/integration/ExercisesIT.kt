package com.ludocode.ludocodebackend.catalog.integration

import com.jayway.jsonpath.TypeRef
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseTreeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
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

        val lesson1: Lesson = pyModule1Lessons[0]
        val lesson2: Lesson = pyModule2Lessons[0]

        val exercises = exerciseRepository.saveAll(listOf(
            Exercise(title = "Complete the expression", prompt = "let sum = ___ + 4", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),
            Exercise(title = "Create a variable with a value of 'House'", prompt = "const ___ = ___", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),

            Exercise(title = "What will the following code return", prompt = "const score = 4 + 4;", exerciseType = ExerciseType.ANALYZE, lessonId = lesson2.id),
            Exercise(title = "Which of the following declares a variable that can not be reassigned", exerciseType = ExerciseType.TRIVIA, lessonId = lesson2.id),
        ))

        val exerciseOptions = exerciseOptionRepository.saveAll(listOf(
            ExerciseOption(content = "4", answerOrder = 1, exerciseId = exercises[0].id),
            ExerciseOption(content = "4", answerOrder = null, exerciseId = exercises[0].id),

            ExerciseOption(content = "house", answerOrder = 1, exerciseId = exercises[1].id),
            ExerciseOption(content = "'house'", answerOrder = 2, exerciseId = exercises[1].id),

            ExerciseOption(content = "8", answerOrder = 1, exerciseId = exercises[2].id),
            ExerciseOption(content = "undefined", answerOrder = null, exerciseId = exercises[2].id),

            ExerciseOption(content = "let", answerOrder = 1, exerciseId = exercises[3].id),
            ExerciseOption(content = "const", answerOrder = null, exerciseId = exercises[3].id),
        ))

        val response: List<ExerciseResponse> = submitGetExercisesByLessonId(lesson1.id!!)
        val response2: List<ExerciseResponse> = submitGetExercisesByLessonId(lesson2.id!!)

        assertThat(response).isNotEmpty()
        assertThat(response2).isNotEmpty()

        for (res: ExerciseResponse in response) {

            assertThat(res.exerciseOptions.size).isEqualTo(2)
            assertThat(res.lessonId).isEqualTo(lesson1.id)
            assertThat(res.title).isNotNull()
            assertThat(res.prompt).isNotNull()
            assertThat(res.exerciseType).isEqualTo(ExerciseType.CLOZE)
        }

        for (res: ExerciseResponse in response2) {
            assertThat(res.exerciseOptions.size).isEqualTo(2)
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