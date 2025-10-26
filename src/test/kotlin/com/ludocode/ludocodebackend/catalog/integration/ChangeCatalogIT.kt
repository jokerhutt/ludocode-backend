package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ExerciseDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ExerciseOptionDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.LessonDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ModuleDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.response.CCLessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.admin.response.CCModuleResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.commons.constants.PathConstants.ADMIN
import com.ludocode.ludocodebackend.commons.constants.PathConstants.CHANGE_CATALOG
import com.ludocode.ludocodebackend.commons.constants.PathConstants.UPDATE_COURSE
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UpdatedCourseResponse
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class ChangeCatalogIT : AbstractIntegrationTest() {



    @BeforeEach
    fun seed () {

    }

    @Test
    fun submitChangeLessons_noDeletions_returnsChanged () {

        val targetModule: Module = pyModule1
        val targetModulessons = pyModule1Lessons

        val lesson1: Lesson = targetModulessons[0]
        val lesson2: Lesson = targetModulessons[1]

        val exercises = exerciseRepository.saveAll(listOf(
            Exercise(exerciseId = ExerciseId(UUID.randomUUID(), 1), title = "Complete the expression", prompt = "let sum = ___ + 4", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),
            Exercise(exerciseId = ExerciseId(UUID.randomUUID(), 1), title = "Create a variable with a value of 'House'", prompt = "const ___ = ___", exerciseType = ExerciseType.CLOZE, lessonId = lesson1.id),

            Exercise(exerciseId = ExerciseId(UUID.randomUUID(), 1), title = "What will the following code return", prompt = "const score = 4 + 4;", exerciseType = ExerciseType.ANALYZE, lessonId = lesson2.id),
            Exercise(exerciseId = ExerciseId(UUID.randomUUID(), 1), title = "Which of the following declares a variable that can not be reassigned", exerciseType = ExerciseType.TRIVIA, lessonId = lesson2.id),
        ))

        val exerciseOptions = exerciseOptionRepository.saveAll(listOf(
            ExerciseOption(content = "4", answerOrder = 1, exerciseId = exercises[0].exerciseId.id, exerciseVersion = 1),
            ExerciseOption(content = "4", answerOrder = null, exerciseId = exercises[0].exerciseId.id, exerciseVersion = 1),

            ExerciseOption(content = "house", answerOrder = 1, exerciseId = exercises[1].exerciseId.id, exerciseVersion = 1),
            ExerciseOption(content = "'house'", answerOrder = 2, exerciseId = exercises[1].exerciseId.id, exerciseVersion = 1),

            ExerciseOption(content = "8", answerOrder = 1, exerciseId = exercises[2].exerciseId.id, exerciseVersion = 1),
            ExerciseOption(content = "undefined", answerOrder = null, exerciseId = exercises[2].exerciseId.id, exerciseVersion = 1),

            ExerciseOption(content = "let", answerOrder = 1, exerciseId = exercises[3].exerciseId.id, exerciseVersion = 1),
            ExerciseOption(content = "const", answerOrder = null, exerciseId = exercises[3].exerciseId.id, exerciseVersion = 1),
        ))

        val l1ExerciseToDelete = exercises.get(1)
        val l1ExerciseToModify = exercises.get(0)







        val newOptions = listOf(
            ExerciseOptionDiffRequest(exerciseOptions[0].id!!, content = "newcontent", answerOrder = null),
            ExerciseOptionDiffRequest(exerciseOptions[1].id!!, content = "house", answerOrder = 1)
        )
        val exerciseDiffRequest = listOf(
            ExerciseDiffRequest(l1ExerciseToModify.exerciseId.id, "l1e1", l1ExerciseToModify.prompt!!, l1ExerciseToModify.exerciseType, l1ExerciseToModify.exerciseId.version, options = newOptions)
        )

        val lessonDiffRequests = listOf(
            LessonDiffRequest(lesson1.id!!, title = "l1", changedExercises = exerciseDiffRequest, exercisesToDelete = listOf(l1ExerciseToDelete.exerciseId.id))
        )

        val moduleDifReq = ModuleDiffRequest(targetModule.id!!, "New Title", orderByIds = listOf(lesson2.id!!, lesson1.id!!, pyModule1Lessons[2].id!!, pyModule1Lessons[3].id!!), lessonDiffRequests, lessonsToDelete = listOf())

        val res = submitPostUpdateCatalog(req = moduleDifReq)

        assertThat(res).isNotNull()

        val resModule = res.module
        assertThat(resModule.title).isNotEqualTo(targetModule.title)
        assertThat(resModule.title).isEqualTo("New Title")

        for (ccLesson: CCLessonResponse in res.lessons) {
            val lesson = ccLesson.lesson
            val exercises = ccLesson.exercises
            assertThat(exercises).isNotNull()

            if (lesson.id == lesson1.id) {
                assertThat(lesson.title).isEqualTo("l1")
                assertThat(lesson.orderIndex).isEqualTo(2)
                for (exercise: ExerciseResponse in exercises) {

                   assertThat(exercise.id).isNotEqualTo(l1ExerciseToDelete.exerciseId.id)

                   if (exercise.id == l1ExerciseToModify.exerciseId.id) {
                       assertThat(exercise.title).isEqualTo("l1e1")
                       assertThat(exercise.prompt).isEqualTo(l1ExerciseToModify.prompt)
                       assertThat(exercise.version).isEqualTo(2)

                   }
                }
            }

            if (lesson.id == lesson2.id) {
                assertThat(lesson.orderIndex).isEqualTo(1)
            }

        }



    }

    private fun submitPostUpdateCatalog(req: ModuleDiffRequest): CCModuleResponse =
        given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body(req)
            .`when`()
            .post("$ADMIN$CHANGE_CATALOG")
            .then()
            .statusCode(200)
            .extract()
            .`as`(CCModuleResponse::class.java)





}