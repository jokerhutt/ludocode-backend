package com.ludocode.ludocodebackend.catalog.integration
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.util.CatalogChangeTestUtil
import com.ludocode.ludocodebackend.support.util.CourseProgressTestUtil
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.support.util.LessonSubmissionTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.util.UUID

class ChangeCatalogIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @BeforeEach
    fun seed () {

    }

    @Test
    fun submitCurriculumChange_returnsChanged() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.toCurriculumDraft(pythonSnap)

        val moduleIndex = 1

        // Change existing lesson title
        pythonCurriculum.modules[moduleIndex].lessons[0].title = "First lesson title"

        // Add new lessons to existing module
        pythonCurriculum.modules[moduleIndex].lessons += listOf(
            CatalogChangeTestUtil.createLesson("New Lesson"),
            CatalogChangeTestUtil.createLesson("New Lesson")
        )

        // Add new module with lessons
        pythonCurriculum.modules += CatalogChangeTestUtil.createModule(
            "New Module",
            "New Module Lesson",
            "New Module Lesson Two"
        )

        val result = submitPostUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(pythonCurriculum)
    }

    @Test
    fun submitCurriculumChangeWithDeletions_returnsChangedWithDeletions() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.toCurriculumDraft(pythonSnap)

        val moduleIndex = 1

        // Change existing lesson title
        pythonCurriculum.modules[moduleIndex].lessons[0].title = "First lesson title"

        // Add new lessons
        pythonCurriculum.modules[moduleIndex].lessons += listOf(
            CatalogChangeTestUtil.createLesson("New Lesson"),
            CatalogChangeTestUtil.createLesson("New Lesson")
        )

        // Add new module
        pythonCurriculum.modules += CatalogChangeTestUtil.createModule(
            "New Module",
            "New Module Lesson",
            "New Module Lesson Two"
        )

        // Delete lesson and module
        val lessonToDelete = pythonCurriculum.modules[0].lessons[0]
        val moduleToDelete = pythonCurriculum.modules[1]

        pythonCurriculum.modules[0].lessons -= lessonToDelete
        pythonCurriculum.modules -= moduleToDelete

        val result = submitPostUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(pythonCurriculum)
    }

    @Test
    fun submitExercisesChangeWithDeletions_returnsChangedWithDeletions() {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonToChange = pythonSnap.modules[0].lessons[0]

        val lessonCurriculum = LessonCurriculumDraftSnapshot(
            exercises = lessonToChange.exercises
        )

        // Change existing exercise
        val exerciseToChange = lessonCurriculum.exercises[1]
        exerciseToChange.title = "Changed title"
        CatalogChangeTestUtil.updateExerciseOptions(
            exerciseToChange,
            correctAnswers = listOf("mouse"),
            distractors = listOf("'mouse'")
        )

        // Delete one exercise
        val exerciseToDelete = lessonCurriculum.exercises[0]
        lessonCurriculum.exercises -= exerciseToDelete

        // Add new exercises
        lessonCurriculum.exercises += CatalogChangeTestUtil.createInfoExercise("New INFO lesson")
        lessonCurriculum.exercises += CatalogChangeTestUtil.createClozeExercise(
            title = "New CLOZE lesson",
            subtitle = "You must wrap text in quotes",
            prompt = "print(___i love python___)",
            correctAnswers = listOf("'", "'"),
            distractors = listOf("+"),
            media = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQIbjxOIxdHAylWUgy-LqVNWa9ID3VmUy8Lxg&s"
        )

        val result = submitPostUpdateExerciseCatalog(lessonToChange.id, lessonCurriculum)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(".*exerciseOptionId")
            .isEqualTo(lessonCurriculum)
    }

    @Test
    fun submitExercisesChangeWithDeletions_preservesCourseCompletion () {
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val lessonToChange = pythonSnap.modules[0].lessons[0]

        val lessonCurriculum = LessonCurriculumDraftSnapshot(
            exercises = lessonToChange.exercises
        )

        courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, pythonId), currentModuleId = pyMod2Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftId),  currentModuleId = swMod2Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
        ))

        val completeProgress = lessonCompletionRepository.saveAll(CourseProgressTestUtil.pythonProgress(
            user1.id, pythonId, pythonLessons
        ))

        lessonCompletionRepository.saveAll(completeProgress)

        // Change existing exercise
        val exerciseToChange = lessonCurriculum.exercises[1]
        exerciseToChange.title = "Changed title"
        CatalogChangeTestUtil.updateExerciseOptions(
            exerciseToChange,
            correctAnswers = listOf("mouse"),
            distractors = listOf("'mouse'")
        )

        // Delete one exercise
        val exerciseToDelete = lessonCurriculum.exercises[0]
        lessonCurriculum.exercises -= exerciseToDelete

        // Add new exercises
        lessonCurriculum.exercises += CatalogChangeTestUtil.createInfoExercise("New INFO lesson")
        lessonCurriculum.exercises += CatalogChangeTestUtil.createClozeExercise(
            title = "New CLOZE lesson",
            subtitle = "You must wrap text in quotes",
            prompt = "print(___i love python___)",
            correctAnswers = listOf("'", "'"),
            distractors = listOf("+"),
            media = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQIbjxOIxdHAylWUgy-LqVNWa9ID3VmUy8Lxg&s"
        )

        val result = submitPostUpdateExerciseCatalog(lessonToChange.id, lessonCurriculum)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(".*exerciseOptionId")
            .isEqualTo(lessonCurriculum)

        val response = LessonSubmissionTestUtil.completeLesson(user1.id, pythonSnap.modules[1].lessons[1], pythonId, true)

        assertThat(response).isNotNull()

        val content = response.content!!
        assertThat(response.status).isEqualTo(LessonCompletionStatus.COURSE_COMPLETE)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2Id)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)
    }

    @Test
    fun submitCurriculumChange_addsLessons_courseIncomplete() {

        courseProgressRepository.saveAll(listOf(
            CourseProgress(id = CourseProgressId(user1.id!!, pythonId), isComplete = true, currentModuleId = pyMod2Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock)),
            CourseProgress(id = CourseProgressId(user1.id!!, swiftId), isComplete = true,  currentModuleId = swMod2Id, createdAt = OffsetDateTime.now(clock), updatedAt = OffsetDateTime.now(clock))
        ))

        val completeProgress = lessonCompletionRepository.saveAll(CourseProgressTestUtil.pythonProgress(
            user1.id, pythonId, pythonLessons
        ))

        lessonCompletionRepository.saveAll(completeProgress)

        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)
        val pythonCurriculum = CatalogChangeTestUtil.toCurriculumDraft(pythonSnap)

        val moduleIndex = 1

        // Change existing lesson title
        pythonCurriculum.modules[moduleIndex].lessons[0].title = "First lesson title"

        // Add new lessons
        pythonCurriculum.modules[moduleIndex].lessons += listOf(
            CatalogChangeTestUtil.createLesson("New Lesson"),
            CatalogChangeTestUtil.createLesson("New Lesson")
        )

        // Add new module
        pythonCurriculum.modules += CatalogChangeTestUtil.createModule(
            "New Module",
            "New Module Lesson",
            "New Module Lesson Two"
        )

        val result = submitPostUpdateCurriculum(pythonCurriculum, pythonId)

        assertThat(result).isNotNull()
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(pythonCurriculum)

        val response = LessonSubmissionTestUtil.completeLesson(user1.id, pythonSnap.modules[1].lessons[1], pythonId, true)

        assertThat(response).isNotNull()

        val content = response.content!!
        assertThat(response.status).isEqualTo(LessonCompletionStatus.OK)
        assertThat(content.newCourseProgress.courseId).isEqualTo(pythonId)
        assertThat(content.newCourseProgress.moduleId).isEqualTo(pyMod2Id)
        assertThat(content.updatedCompletedLesson.isCompleted).isEqualTo(true)

    }

    private fun submitPostUpdateCurriculum(req: CurriculumDraftSnapshot, courseId: UUID) : CurriculumDraftSnapshot =
        TestRestClient.putOk(ApiPaths.SNAPSHOTS.byCourseCurriculumAdmin(courseId), user1.id, req,
            CurriculumDraftSnapshot::class.java)

    private fun submitPostUpdateExerciseCatalog(lessonId: UUID, req: LessonCurriculumDraftSnapshot): LessonCurriculumDraftSnapshot =
        TestRestClient.putOk(ApiPaths.LESSONS.byAdminId(lessonId), user1.id!!, req, LessonCurriculumDraftSnapshot::class.java)

}