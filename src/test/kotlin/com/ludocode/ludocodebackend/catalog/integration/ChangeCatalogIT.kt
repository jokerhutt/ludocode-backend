package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotBuilderService
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SNAPSHOT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SUBMIT_COURSE_SNAPSHOT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SUBMIT_MODULE_SNAPSHOT
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChangeCatalogIT : AbstractIntegrationTest() {


    @Autowired
    private lateinit var snapshotBuilderService: SnapshotBuilderService

    @BeforeEach
    fun seed () {

    }

    @Test
    fun submitCourseChange_OneModuleChanged_returnsChanged() {
        val pythonSnap = snapshotBuilderService.buildCourseSnapshot(pythonId)

        // capture "before" counts
        val initialModule = pythonSnap.modules.first()
        val initialLessonCount = initialModule.lessons.size
        val lessonToChange = initialModule.lessons[0]
        val lessonToDelete = initialModule.lessons[1]
        val initialExerciseCountInChanged = lessonToChange.exercises.size
        val exerciseToChange = lessonToChange.exercises[0]
        val exerciseToDelete = lessonToChange.exercises[1]

        // build mutated lesson
        val mutatedLesson =
            lessonToChange.copy(
                title = "New Title",
                exercises = lessonToChange.exercises
                    .filter { it.id != exerciseToDelete.id }
                    .map { ex ->
                        if (ex.id == exerciseToChange.id) ex.copy(title = "Waka waka", subtitle = "Waka") else ex
                    }
            )

        // build mutated module
        val mutatedModule =
            initialModule.copy(
                lessons = initialModule.lessons
                    .filter { it.id != lessonToDelete.id }
                    .map { ls -> if (ls.id == mutatedLesson.id) mutatedLesson else ls }
            )

        // build mutated course
        val mutatedCourse =
            pythonSnap.copy(
                modules = pythonSnap.modules.map { m ->
                    if (m.moduleId == mutatedModule.moduleId) mutatedModule else m
                }
            )

        // send
        val res = submitPostUpdateCatalog(mutatedCourse)

        // assert
        assertThat(res).isNotNull
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

        // sanity: deleted ids are gone
        assertThat(changedModule.lessons.any { it.id == lessonToDelete.id }).isFalse
        assertThat(changedLessonNew.exercises.any { it.id == exerciseToDelete.id }).isFalse
    }

    private fun submitPostUpdateCatalog(req: CourseSnap): CourseSnap =
        given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body(req)
            .`when`()
            .post("$SNAPSHOT$SUBMIT_COURSE_SNAPSHOT")
            .then()
            .statusCode(200)
            .extract()
            .`as`(CourseSnap::class.java)

}