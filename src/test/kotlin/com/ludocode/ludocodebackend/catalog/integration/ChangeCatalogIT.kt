package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SNAPSHOT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SUBMIT_MODULE_SNAPSHOT
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
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

        val l1ExerciseToDelete = exercises[1]
        val l1ExerciseToModify = exercises[0]

// new lesson to add
        val l5ExerciseToAddOptions = listOf(
            OptionSnap( content = "newOption", answerOrder = null),
            OptionSnap( content = "world", answerOrder = 1)
        )
        val l5ExerciseToAdd = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "newLessonExercise",
            prompt = "hello ___",
            exerciseType = ExerciseType.CLOZE,
            correctOptions = l5ExerciseToAddOptions.filter { opt -> opt.answerOrder != null },
            distractors = l5ExerciseToAddOptions.filter {opt -> opt.answerOrder == null}
        )
        val l5LessonToAdd = LessonSnap(
            id = null,
            tempId = UUID.randomUUID(),
            title = "newLesson",
            exercises = listOf(l5ExerciseToAdd),
            orderIndex = 5
        )

// existing lesson1: modify one exercise + add one exercise
        val l1ExerciseToAddOptions = listOf(
            OptionSnap(content = "meow", answerOrder = null),
            OptionSnap(content = "kachow", answerOrder = 1)
        )
        val l1ExerciseToAdd = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "newLessonExercise",
            prompt = "hello ___",
            exerciseType = ExerciseType.CLOZE,
            correctOptions = l1ExerciseToAddOptions.filter { opt -> opt.answerOrder != null },
            distractors = l1ExerciseToAddOptions.filter {opt -> opt.answerOrder == null}
        )
        val newOptions = listOf(
            OptionSnap( content = "newcontent", answerOrder = null),
            OptionSnap(content = "house", answerOrder = 1)
        )
        val exerciseSnapsForL1 = listOf(
            ExerciseSnap(
                id = l1ExerciseToModify.exerciseId.id,
                title = "l1e1",
                prompt = l1ExerciseToModify.prompt!!,
                exerciseType = l1ExerciseToModify.exerciseType,
                correctOptions = newOptions.filter { opt -> opt.answerOrder != null },
                distractors = newOptions.filter {opt -> opt.answerOrder == null}
            ),
            l1ExerciseToAdd
        )
        val lesson1Snap = LessonSnap(
            id = lesson1.id!!,
            tempId = lesson1.id!!,
            title = "l1",
            exercises = exerciseSnapsForL1,
            orderIndex = 2
        )

// unchanged lessons to preserve order
        val lesson2Snap = LessonSnap(
            id = lesson2.id!!,
            tempId = lesson2.id!!,
            title = lesson2.title,
            exercises = emptyList(),
            orderIndex = 1
        )
        val lesson3Snap = LessonSnap(
            id = pyModule1Lessons[2].id!!,
            tempId = pyModule1Lessons[2].id!!,
            title = pyModule1Lessons[2].title,
            exercises = emptyList(),
            orderIndex = 3
        )
        val lesson4Snap = LessonSnap(
            id = pyModule1Lessons[3].id!!,
            tempId = pyModule1Lessons[3].id!!,
            title = pyModule1Lessons[3].title,
            exercises = emptyList(),
            orderIndex = 4
        )

// snapshot in desired order: [lesson2, lesson1, lesson3, lesson4, new lesson]
        val moduleDifReq = ModuleSnapshot(
            moduleId = targetModule.id!!,
            tempId = targetModule.id!!,
            title = "New Title",
            lessons = listOf(lesson2Snap, lesson1Snap, lesson3Snap, lesson4Snap, l5LessonToAdd)
        )

        val res = submitPostUpdateCatalog(req = moduleDifReq)

        assertThat(res).isNotNull()

        assertThat(res.title).isNotEqualTo(targetModule.title)
        assertThat(res.title).isEqualTo("New Title")
        assertThat(res.lessons.any { it.title == "newLesson" }).isTrue

        for (lesson: LessonSnap in res.lessons) {

            if (lesson.id == lesson1.id) {
                assertThat(lesson.title).isEqualTo("l1")
                assertThat(lesson.orderIndex).isEqualTo(2)

                assertThat(lesson.exercises.map { it.id })
                    .contains(l1ExerciseToAdd.id)

                for (exercise: ExerciseSnap in lesson.exercises) {

                    assertThat(exercise.id).isNotEqualTo(l1ExerciseToDelete.exerciseId.id)

                    if (exercise.id == l1ExerciseToModify.exerciseId.id) {
                        assertThat(exercise.title).isEqualTo("l1e1")
                        assertThat(exercise.prompt).isEqualTo(l1ExerciseToModify.prompt)
                    }

                    if (exercise.id == l1ExerciseToAdd.id) {
                        assertThat(exercise.title).isEqualTo(l1ExerciseToAdd.title)
                        assertThat(exercise.prompt).isEqualTo(l1ExerciseToAdd.prompt)
                        assertThat(exercise.correctOptions.size).isEqualTo(1)
                        assertThat(exercise.distractors.size).isEqualTo(1)
                    }
                }
            }

            if (lesson.title == l5ExerciseToAdd.title) {
                assertThat(lesson.orderIndex).isEqualTo(5)
                assertThat(lesson.exercises.size).isEqualTo(1)
                for (exercise: ExerciseSnap in lesson.exercises) {
                    assertThat(exercise.prompt).isEqualTo("hello ___")
                    assertThat(exercise.correctOptions.size).isEqualTo(1)
                    assertThat(exercise.correctOptions.size).isEqualTo(1)
                }
            }

            if (lesson.id == lesson2.id) {
                assertThat(lesson.orderIndex).isEqualTo(1)
            }
        }



    }

    private fun submitPostUpdateCatalog(req: ModuleSnapshot): ModuleSnapshot =
        given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body(req)
            .`when`()
            .post("$SNAPSHOT$SUBMIT_MODULE_SNAPSHOT")
            .then()
            .statusCode(200)
            .extract()
            .`as`(ModuleSnapshot::class.java)

}