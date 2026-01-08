package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotBuilderService
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class ChangeCatalogIT : AbstractIntegrationTest() {


    @Autowired
    private lateinit var snapshotBuilderService: SnapshotBuilderService

    @BeforeEach
    fun seed () {

    }

    @Test
    fun submitCourseChange_returnsChanged() {
        val pythonSnap = snapshotBuilderService.buildCourseSnapshot(pythonId)

        val initialModule = pythonSnap.modules.first()
        val moduleToDelete = pythonSnap.modules[1]
        val initialModuleCount = pythonSnap.modules.size
        val initialLessonCount = initialModule.lessons.size
        val lessonToChange = initialModule.lessons[0]
        val lessonToDelete = initialModule.lessons[1]
        val initialExerciseCountInChanged = lessonToChange.exercises.size
        val exerciseToChange = lessonToChange.exercises[0]
        val exerciseToDelete = lessonToChange.exercises[1]

        val newExerciseId = UUID.randomUUID()

        val exerciseToAdd = ExerciseSnap(
            id = newExerciseId,
            title = "New Exercise",
            subtitle = "New subtitle",
            prompt = "___'Hello world')",
            exerciseType = ExerciseType.CLOZE,
            correctOptions = listOf(OptionSnap(content = "(", answerOrder = 1, exerciseOptionId = UUID.randomUUID())),
            distractors = listOf(OptionSnap(content = ")", answerOrder = null, exerciseOptionId = UUID.randomUUID()))
        )

        val mutatedLesson =
            lessonToChange.copy(
                title = "New Title",
                exercises = lessonToChange.exercises
                    .filter { it.id != exerciseToDelete.id }
                    .map { ex ->
                        if (ex.id == exerciseToChange.id) ex.copy(title = "Waka waka", subtitle = "Waka") else ex
                    }
            )

        val mutatedModule =
            initialModule.copy(
                lessons = initialModule.lessons
                    .filter { it.id != lessonToDelete.id }
                    .map { ls -> if (ls.id == mutatedLesson.id) mutatedLesson else ls }
            )

        val mutatedCourse =
            pythonSnap.copy(
                modules = pythonSnap.modules
                    .filter { it.moduleId != moduleToDelete.moduleId } // remove module entirely
                    .map { m -> if (m.moduleId == mutatedModule.moduleId) mutatedModule else m }
            )

        val res = submitPostUpdateCatalog(mutatedCourse)

        assertThat(res).isNotNull

        assertThat(res.modules.size).isEqualTo(initialModuleCount - 1)
        val changedModule = res.modules.first { it.moduleId == mutatedModule.moduleId }

        assertThat(changedModule.lessons.size)
            .isEqualTo(initialLessonCount - 1)

        val changedLessonNew = changedModule.lessons.first { it.id == mutatedLesson.id }
        assertThat(changedLessonNew.title).isEqualTo("New Title")
        assertThat(changedLessonNew.exercises.size)
            .isEqualTo(initialExerciseCountInChanged - 1)

        val changedExercise = changedLessonNew.exercises.first { it.id == exerciseToChange.id }
        assertThat(changedExercise.title).isEqualTo("Waka waka")
        assertThat(changedExercise.subtitle).isEqualTo("Waka")

        assertThat(changedModule.lessons.any { it.id == lessonToDelete.id }).isFalse
        assertThat(changedLessonNew.exercises.any { it.id == exerciseToDelete.id }).isFalse
    }

    private fun submitPostUpdateCatalog(req: CourseSnap): CourseSnap =
        TestRestClient.putOk(ApiPaths.SNAPSHOTS.byCourse(req.courseId), user1.id!!, req, CourseSnap::class.java)

}