package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlLesson
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlModule
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlRoot
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ClozeInteraction
import com.ludocode.ludocodebackend.lesson.domain.jsonb.HeaderBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InteractionBlank
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InteractionFile
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ParagraphBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.SelectInteraction
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.support.util.CatalogChangeTestUtil
import com.ludocode.ludocodebackend.support.util.CourseProgressTestUtil
import com.ludocode.ludocodebackend.support.util.LessonSubmissionTestUtil
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepetitionInfo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.util.*

class ChangeCatalogIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @BeforeEach
    fun seed() {

    }

    @Test
    fun submitCurriculumChange_returnsChanged() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.toCurriculumDraft(pythonSnap)

        val moduleIndex = 1

        pythonCurriculum.modules[moduleIndex].lessons[0].title = "First lesson title"

        pythonCurriculum.modules[moduleIndex].lessons += listOf(
            CatalogChangeTestUtil.createLesson("New Lesson"),
            CatalogChangeTestUtil.createLesson("New Lesson")
        )

        pythonCurriculum.modules += CatalogChangeTestUtil.createModule(
            "New Module",
            "New Module Lesson",
            "New Module Lesson Two"
        )

        val result = submitPutUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(pythonCurriculum)
    }

    @Test
    fun submitYamlChangeCourse_changesCourse() {

        val courseToChangeId = pythonId

        val yamlReq = CurriculumYamlRoot(
            title = "Python",
            subjectId = pythonSubject.id,
            languageId = pythonLanguage.id,
            description = "Cool Python Stuff",
            courseType = CourseType.COURSE,
            modules = listOf(
                CurriculumYamlModule(
                    id = null,
                    title = "Printing stuff to the console",
                    lessons = listOf(
                        CurriculumYamlLesson(
                            id = null,
                            title = "Print Statements",
                            exercises = listOf(

                                // INFO exercise
                                ExerciseSnap(
                                    exerciseId = UUID.randomUUID(),
                                    exerciseVersion = 1,
                                    blocks = listOf(
                                        HeaderBlock("Printing in Python"),
                                        ParagraphBlock("Use the print() function to output text to the console.")
                                    ),
                                    interaction = null
                                ),

                                // CLOZE exercise
                                ExerciseSnap(
                                    exerciseId = UUID.randomUUID(),
                                    exerciseVersion = 1,
                                    blocks = listOf(
                                        HeaderBlock("Fill in the missing function"),
                                        ParagraphBlock("Complete the code below.")
                                    ),
                                    interaction = ClozeInteraction(
                                        file = InteractionFile(
                                            language = "python",
                                            content = """
___("Hello world")
""".trimIndent()
                                        ),
                                        blanks = listOf(
                                            InteractionBlank(
                                                index = 0,
                                                correctOptions = listOf("print")
                                            )
                                        ),
                                        options = listOf("print", "echo", "log"),
                                        output = "Hello world"
                                    )
                                )

                            )
                        )
                    )
                )
            )
        )

        submitPutUpdateCurriculumWithYaml(yamlReq, courseToChangeId)

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)

        assertThat(pythonSnap.modules.size).isEqualTo(1)
        assertThat(pythonSnap.modules[0].lessons.size).isEqualTo(1)
        assertThat(pythonSnap.modules[0].lessons[0].exercises.size).isEqualTo(2)
        assertThat(pythonSnap.modules[0].lessons[0].exercises[0].blocks.size).isEqualTo(2)
        assertThat(pythonSnap.modules[0].lessons[0].exercises[1].blocks.size).isEqualTo(2)
        assertThat(pythonSnap.modules[0].lessons[0].exercises[1].interaction).isNotNull()

    }

    @Test
    fun submitCurriculumChangeWithEmptyCourse_throwsError() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.toCurriculumDraft(pythonSnap)

        pythonCurriculum.modules = pythonCurriculum.modules - pythonCurriculum.modules

        assertPutCurriculumError(pythonCurriculum, pythonId, ErrorCode.EMPTY_MODULES)
    }

    @Test
    fun submitCurriculumChangeWithEmptyModule_throwsError() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.toCurriculumDraft(pythonSnap)

        pythonCurriculum.modules[0].lessons = pythonCurriculum.modules[0].lessons - pythonCurriculum.modules[0].lessons

        assertPutCurriculumError(pythonCurriculum, pythonId, ErrorCode.EMPTY_LESSONS)
    }

    @Test
    fun submitLessonCurriculumChangeWithEmptyLesson_throwsError() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonToChange = pythonSnap.modules[0].lessons[0]

        val lessonCurriculum = LessonCurriculumDraftSnapshot(
            exercises = lessonToChange.exercises.toMutableList()
        )

        lessonCurriculum.exercises = lessonCurriculum.exercises - lessonCurriculum.exercises

        assertPutExerciseCatalogError(
            lessonCurriculum,
            lessonToChange.id,
            ErrorCode.EMPTY_EXERCISES
        )
    }

    @Test
    fun submitCurriculumChangeWithDeletions_returnsChangedWithDeletions() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.toCurriculumDraft(pythonSnap)

        val moduleIndex = 1

        pythonCurriculum.modules[moduleIndex].lessons[0].title = "First lesson title"

        pythonCurriculum.modules[moduleIndex].lessons += listOf(
            CatalogChangeTestUtil.createLesson("New Lesson"),
            CatalogChangeTestUtil.createLesson("New Lesson")
        )

        pythonCurriculum.modules += CatalogChangeTestUtil.createModule(
            "New Module",
            "New Module Lesson",
            "New Module Lesson Two"
        )

        val lessonToDelete = pythonCurriculum.modules[0].lessons[0]
        val moduleToDelete = pythonCurriculum.modules[1]

        pythonCurriculum.modules[0].lessons -= lessonToDelete
        pythonCurriculum.modules -= moduleToDelete

        val result = submitPutUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(
                ".*exerciseOptionId",
                ".*exerciseVersion"
            )
            .isEqualTo(pythonCurriculum)
    }

    @Test
    fun submitExercisesChangeWithDeletions_returnsChangedWithDeletions() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonToChange = pythonSnap.modules[0].lessons[0]

        val exercises = lessonToChange.exercises.toMutableList()

        val original = exercises[1]
        exercises[1] = original.copy(
            blocks = listOf(
                HeaderBlock("Changed title")
            ),
            interaction = SelectInteraction(
                items = listOf("mouse", "'mouse'"),
                correctValue = "mouse"
            )
        )

        exercises.removeAt(0)

        // ADD new INFO
        exercises += ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            exerciseVersion = 1,
            blocks = listOf(
                ParagraphBlock("New INFO lesson")
            ),
            interaction = null
        )

        // ADD new CLOZE
        exercises += ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            exerciseVersion = 1,
            blocks = listOf(
                HeaderBlock("New CLOZE lesson"),
                ParagraphBlock("You must wrap text in quotes")
            ),
            interaction = ClozeInteraction(
                file = InteractionFile(
                    language = "python",
                    content = "print(___i love python___)"
                ),
                blanks = listOf(
                    InteractionBlank(0, listOf("'")),
                    InteractionBlank(1, listOf("'"))
                ),
                options = listOf("'", "+"),
                output = "I love python"
            )
        )

        val lessonCurriculum = LessonCurriculumDraftSnapshot(exercises = exercises)

        val result = submitPostUpdateExerciseCatalog(lessonToChange.id, lessonCurriculum)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(
                ".*exerciseOptionId",
                ".*exerciseVersion"
            )
            .isEqualTo(lessonCurriculum)
    }

    @Test
    fun submitExercisesChangeWithDeletions_preservesCourseCompletion() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonToChange = pythonSnap.modules[0].lessons[0]

        val exercises = lessonToChange.exercises.toMutableList()

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(
                    id = CourseProgressId(user1.id!!, pythonId),
                    currentModuleId = pyMod2Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                ),
                CourseProgress(
                    id = CourseProgressId(user1.id!!, swiftId),
                    currentModuleId = swMod2Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                )
            )
        )

        val completeProgress = lessonCompletionRepository.saveAll(
            CourseProgressTestUtil.pythonProgress(
                user1.id, pythonId, pythonLessons
            )
        )
        lessonCompletionRepository.saveAll(completeProgress)

        // MODIFY second exercise
        val original = exercises[1]
        exercises[1] = original.copy(
            blocks = listOf(
                HeaderBlock("Changed title")
            ),
            interaction = SelectInteraction(
                items = listOf("mouse", "'mouse'"),
                correctValue = "mouse"
            )
        )

        // DELETE first
        exercises.removeAt(0)

        // ADD INFO
        exercises += ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            exerciseVersion = 1,
            blocks = listOf(
                ParagraphBlock("New INFO lesson")
            ),
            interaction = null
        )

        // ADD CLOZE
        exercises += ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            exerciseVersion = 1,
            blocks = listOf(
                HeaderBlock("New CLOZE lesson"),
                ParagraphBlock("You must wrap text in quotes")
            ),
            interaction = ClozeInteraction(
                file = InteractionFile(
                    language = "python",
                    content = "print(___i love python___)"
                ),
                blanks = listOf(
                    InteractionBlank(0, listOf("'")),
                    InteractionBlank(1, listOf("'"))
                ),
                options = listOf("'", "+"),
                output = "I love python"
            )
        )

        val lessonCurriculum = LessonCurriculumDraftSnapshot(exercises = exercises)

        val result = submitPostUpdateExerciseCatalog(lessonToChange.id, lessonCurriculum)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(
                ".*exerciseOptionId",
                ".*exerciseVersion"
            )
            .isEqualTo(lessonCurriculum)

        val response =
            LessonSubmissionTestUtil.completeLesson(user1.id, pythonSnap.modules[1].lessons[1], pythonId, true)

        assertThat(response).isNotNull()

        val content = response.content!!
        assertThat(response.status).isEqualTo(LessonCompletionStatus.COURSE_COMPLETE)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2Id)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)
    }

    @Test
    fun submitCurriculumChange_addsLessons_courseIncomplete() {

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(
                    id = CourseProgressId(user1.id!!, pythonId),
                    isComplete = true,
                    currentModuleId = pyMod2Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                ),
                CourseProgress(
                    id = CourseProgressId(user1.id!!, swiftId),
                    isComplete = true,
                    currentModuleId = swMod2Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                )
            )
        )

        val completeProgress = lessonCompletionRepository.saveAll(
            CourseProgressTestUtil.pythonProgress(
                user1.id, pythonId, pythonLessons
            )
        )

        lessonCompletionRepository.saveAll(completeProgress)

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.toCurriculumDraft(pythonSnap)

        val moduleIndex = 1

        pythonCurriculum.modules[moduleIndex].lessons[0].title = "First lesson title"

        pythonCurriculum.modules[moduleIndex].lessons += listOf(
            CatalogChangeTestUtil.createLesson("New Lesson"),
            CatalogChangeTestUtil.createLesson("New Lesson")
        )

        pythonCurriculum.modules += CatalogChangeTestUtil.createModule(
            "New Module",
            "New Module Lesson",
            "New Module Lesson Two"
        )

        val result = submitPutUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(pythonCurriculum)

        val response =
            LessonSubmissionTestUtil.completeLesson(user1.id, pythonSnap.modules[1].lessons[1], pythonId, true)

        assertThat(response).isNotNull()

        val content = response.content!!
        assertThat(response.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2Id)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)

    }

    @RepeatedTest(20, name = "Random Curriculum Changes - Run {currentRepetition}/{totalRepetitions}")
    fun submitRandomCurriculumChanges_returnsChanged(repetitionInfo: RepetitionInfo) {
        val seed = repetitionInfo.currentRepetition.toLong()

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.generateRandomCurriculumChanges(pythonSnap, seed)

        val result = submitPutUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()
        assertThat(result.modules.size).isGreaterThan(0)

        result.modules.forEach { module ->
            assertThat(module.lessons).isNotEmpty()
        }

        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(pythonCurriculum)
    }

    @RepeatedTest(20, name = "Random Exercise Changes - Run {currentRepetition}/{totalRepetitions}")
    fun submitRandomExerciseChanges_returnsChanged(repetitionInfo: RepetitionInfo) {
        val seed = repetitionInfo.currentRepetition.toLong()

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonToChange = pythonSnap.modules[0].lessons[0]

        val modifiedExercises = CatalogChangeTestUtil.generateRandomExerciseChanges(
            lessonToChange.exercises,
            seed
        )

        val lessonCurriculum = LessonCurriculumDraftSnapshot(exercises = modifiedExercises)

        val result = submitPostUpdateExerciseCatalog(lessonToChange.id, lessonCurriculum)

        assertThat(result).isNotNull()
        assertThat(result.exercises.size).isGreaterThan(0)

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(
                ".*exerciseOptionId",
                ".*exerciseVersion"
            )
            .isEqualTo(lessonCurriculum)
    }

    @RepeatedTest(10, name = "Random Curriculum + Exercise Changes - Run {currentRepetition}/{totalRepetitions}")
    fun submitRandomCombinedChanges_preservesProgress(repetitionInfo: RepetitionInfo) {
        val seed = repetitionInfo.currentRepetition.toLong()

        courseProgressRepository.saveAll(
            listOf(
                CourseProgress(
                    id = CourseProgressId(user1.id!!, pythonId),
                    isComplete = false,
                    currentModuleId = pyMod2Id,
                    createdAt = OffsetDateTime.now(clock),
                    updatedAt = OffsetDateTime.now(clock)
                )
            )
        )

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)

        val pythonCurriculum = CatalogChangeTestUtil.generateRandomCurriculumChanges(pythonSnap, seed)
        val curriculumResult = submitPutUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(curriculumResult).isNotNull()
        assertThat(curriculumResult.modules.size).isGreaterThan(0)

        val refreshedSnap = testSnapshotService.buildCourseSnapshot(pythonId)

        if (refreshedSnap.modules.isNotEmpty()) {
            val random = Random(seed)
            val randomModuleIndex = random.nextInt(refreshedSnap.modules.size)
            val randomModule = refreshedSnap.modules[randomModuleIndex]

            if (randomModule.lessons.isNotEmpty()) {
                val randomLessonIndex = random.nextInt(randomModule.lessons.size)
                val lessonToChange = randomModule.lessons[randomLessonIndex]

                val modifiedExercises = CatalogChangeTestUtil.generateRandomExerciseChanges(
                    lessonToChange.exercises,
                    seed + 1000
                )
                val lessonCurriculum = LessonCurriculumDraftSnapshot(exercises = modifiedExercises)
                val exerciseResult = submitPostUpdateExerciseCatalog(lessonToChange.id, lessonCurriculum)

                assertThat(exerciseResult).isNotNull()
                assertThat(exerciseResult.exercises.size).isGreaterThan(0)
            }
        }

        val finalSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        if (finalSnap.modules.isNotEmpty()) {
            val random = Random(seed + 2000)
            val randomModuleIndex = random.nextInt(finalSnap.modules.size)
            val randomModule = finalSnap.modules[randomModuleIndex]

            if (randomModule.lessons.isNotEmpty()) {
                val randomLessonIndex = random.nextInt(randomModule.lessons.size)
                val lessonToComplete = randomModule.lessons[randomLessonIndex]

                val response = LessonSubmissionTestUtil.completeLesson(
                    user1.id,
                    lessonToComplete,
                    pythonId,
                    true
                )

                assertThat(response).isNotNull()
                assertThat(response.content).isNotNull()
            }
        }
    }

    private fun submitPutUpdateCurriculum(req: CurriculumDraftSnapshot, courseId: UUID): CurriculumDraftSnapshot =
        TestRestClient.putOk(
            ApiPaths.SNAPSHOTS.byCourseCurriculumAdmin(courseId), user1.id, req,
            CurriculumDraftSnapshot::class.java
        )

    private fun submitPutUpdateCurriculumWithYaml(
        req: CurriculumYamlRoot,
        courseId: UUID
    ) {
        TestRestClient.putNoContent(
            ApiPaths.SNAPSHOTS.byCourseCurriculumAdmin(courseId) + "?mode=yaml",
            user1.id,
            req,
            "application/x-yaml"
        )
    }

    private fun assertPutCurriculumError(req: CurriculumDraftSnapshot, courseId: UUID, statusCode: ErrorCode): ValidatableResponse? {
        return TestRestClient.assertError("PUT", ApiPaths.SNAPSHOTS.byCourseCurriculumAdmin(courseId), user1.id, req, statusCode)
    }

    private fun assertPutExerciseCatalogError(req: LessonCurriculumDraftSnapshot, lessonId: UUID, statusCode: ErrorCode): ValidatableResponse? {
        return TestRestClient.assertError("PUT", ApiPaths.LESSONS.byAdminId(lessonId), user1.id, req, statusCode)
    }

    private fun submitPostUpdateExerciseCatalog(
        lessonId: UUID,
        req: LessonCurriculumDraftSnapshot
    ): LessonCurriculumDraftSnapshot =
        TestRestClient.putOk(
            ApiPaths.LESSONS.byAdminId(lessonId),
            user1.id!!,
            req,
            LessonCurriculumDraftSnapshot::class.java
        )

}