package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.app.service.SnapshotBuilderService
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
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
                    .filter { it.moduleId != moduleToDelete.moduleId }
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

    @Test
    fun submitCurriculumChange_returnsChanged() {
        val pythonSnap = snapshotBuilderService.buildCourseSnapshot(pythonId)

        val pythonCurriculum = CurriculumDraftSnapshot(
            modules = pythonSnap.modules.map { module ->
                ModuleDraftSnapshot(
                    id = module.moduleId,
                    title = module.title,
                    lessons = module.lessons.map { lesson ->
                        LessonDraftSnapshot(
                            id = lesson.id,
                            title = lesson.title
                        )
                    }
                )
            }
        )

        val moduleIndex = 1

        val lessonToChange =
            pythonCurriculum.modules[moduleIndex].lessons[0]

        lessonToChange.title = "First lesson title"

        val firstLessonToAdd = LessonDraftSnapshot(
            id = UUID.randomUUID(),
            title = "New Lesson"
        )

        val secondLessonToAdd = LessonDraftSnapshot(
            id = UUID.randomUUID(),
            title = "New Lesson"
        )

        pythonCurriculum.modules[moduleIndex].lessons =
            pythonCurriculum.modules[moduleIndex].lessons +
                    listOf(firstLessonToAdd, secondLessonToAdd)

        val moduleToAdd = ModuleDraftSnapshot(
            id = UUID.randomUUID(),
            title = "New Module",
            lessons = listOf(
                LessonDraftSnapshot(
                    id = UUID.randomUUID(),
                    title = "New Module Lesson"
                ),
                LessonDraftSnapshot(
                    id = UUID.randomUUID(),
                    title = "New Module Lesson Two"
                )
            )
        )

        pythonCurriculum.modules =
            pythonCurriculum.modules + moduleToAdd

        val result =
            submitPostUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()

        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(pythonCurriculum)

    }

    @Test
    fun submitCurriculumChangeWithDeletions_returnsChangedWithDeletions() {
        val pythonSnap = snapshotBuilderService.buildCourseSnapshot(pythonId)

        val pythonCurriculum = CurriculumDraftSnapshot(
            modules = pythonSnap.modules.map { module ->
                ModuleDraftSnapshot(
                    id = module.moduleId,
                    title = module.title,
                    lessons = module.lessons.map { lesson ->
                        LessonDraftSnapshot(
                            id = lesson.id,
                            title = lesson.title
                        )
                    }
                )
            }
        )

        val moduleIndex = 1

        val lessonToChange =
            pythonCurriculum.modules[moduleIndex].lessons[0]

        lessonToChange.title = "First lesson title"

        val firstLessonToAdd = LessonDraftSnapshot(
            id = UUID.randomUUID(),
            title = "New Lesson"
        )

        val secondLessonToAdd = LessonDraftSnapshot(
            id = UUID.randomUUID(),
            title = "New Lesson"
        )

        val lessonToDelete = pythonCurriculum.modules[0].lessons[0]
        val moduleToDelete = pythonCurriculum.modules[1]

        pythonCurriculum.modules[moduleIndex].lessons =
            pythonCurriculum.modules[moduleIndex].lessons +
                    listOf(firstLessonToAdd, secondLessonToAdd)

        val moduleToAdd = ModuleDraftSnapshot(
            id = UUID.randomUUID(),
            title = "New Module",
            lessons = listOf(
                LessonDraftSnapshot(
                    id = UUID.randomUUID(),
                    title = "New Module Lesson"
                ),
                LessonDraftSnapshot(
                    id = UUID.randomUUID(),
                    title = "New Module Lesson Two"
                )
            )
        )

        pythonCurriculum.modules =
            pythonCurriculum.modules + moduleToAdd

        pythonCurriculum.modules[0].lessons =
            pythonCurriculum.modules[0].lessons - lessonToDelete

        pythonCurriculum.modules =
            pythonCurriculum.modules - moduleToDelete

        val result =
            submitPostUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()

        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(pythonCurriculum)

    }

    @Test
    fun submitExercisesChangeWithDeletions_returnsChangedWithDeletions() {
        val pythonSnap = snapshotBuilderService.buildCourseSnapshot(pythonId)

        val lessonToChange = pythonSnap.modules[0].lessons[0]

        val lessonCurriculum = LessonCurriculumDraftSnapshot(
            exercises = lessonToChange.exercises
        )

        val exerciseToChange = lessonCurriculum.exercises[1]
        exerciseToChange.title = "Changed title"
        exerciseToChange.correctOptions = listOf(OptionSnap(
            content = "mouse",
            answerOrder = 1,
            exerciseOptionId = UUID.randomUUID()
        ))
        exerciseToChange.distractors = listOf(OptionSnap(
            content = "'mouse'",
            answerOrder = null,
            exerciseOptionId = UUID.randomUUID()
        ))


        val exerciseToDelete = lessonCurriculum.exercises[0]
        val exerciseToAdd1 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "New INFO exercise",
            subtitle = null,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.INFO,
            correctOptions = listOf(),
            distractors = listOf()
        )
        val exerciseToAdd2 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "New CLOZE exercise",
            subtitle = "You must wrap text in quotes",
            prompt = "print(___i love python___)",
            media = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQIbjxOIxdHAylWUgy-LqVNWa9ID3VmUy8Lxg&s",
            exerciseType = ExerciseType.CLOZE,
            correctOptions = listOf(OptionSnap("'", exerciseOptionId = UUID.randomUUID(), answerOrder = 1), OptionSnap("'", exerciseOptionId = UUID.randomUUID(), answerOrder = 2)),
            distractors = listOf(OptionSnap("+", exerciseOptionId = UUID.randomUUID(), answerOrder = null))
        )

        lessonCurriculum.exercises = lessonCurriculum.exercises - exerciseToDelete
        lessonCurriculum.exercises = lessonCurriculum.exercises + exerciseToAdd1
        lessonCurriculum.exercises = lessonCurriculum.exercises + exerciseToAdd2

        val result = submitPostUpdateExerciseCatalog(lessonToChange.id, lessonCurriculum)

        assertThat(result).isNotNull()

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(".*exerciseOptionId")
            .isEqualTo(lessonCurriculum)

    }





    private fun submitPostUpdateCurriculum(req: CurriculumDraftSnapshot, courseId: UUID) : CurriculumDraftSnapshot =
        TestRestClient.putOk(ApiPaths.SNAPSHOTS.byCourseCurriculumAdmin(courseId), user1.id, req,
            CurriculumDraftSnapshot::class.java)

    private fun submitPostUpdateCatalog(req: CourseSnap): CourseSnap =
        TestRestClient.putOk(ApiPaths.SNAPSHOTS.byCourseAdmin(req.courseId), user1.id!!, req, CourseSnap::class.java)

    private fun submitPostUpdateExerciseCatalog(lessonId: UUID, req: LessonCurriculumDraftSnapshot): LessonCurriculumDraftSnapshot =
        TestRestClient.putOk(ApiPaths.SNAPSHOTS.byLessonCurriculumAdmin(lessonId), user1.id!!, req, LessonCurriculumDraftSnapshot::class.java)



}