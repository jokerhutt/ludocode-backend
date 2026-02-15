package com.ludocode.ludocodebackend.catalog.integration
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.util.CourseProgressTestUtil
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import com.ludocode.ludocodebackend.support.util.LessonSubmissionTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
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
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)

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
        val pythonSnap = testSnapshotService.buildCourseSnapshot(pythonId)

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
            title = "New INFO lesson",
            subtitle = null,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.INFO,
            correctOptions = listOf(),
            distractors = listOf()
        )
        val exerciseToAdd2 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "New CLOZE lesson",
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
            title = "New INFO lesson",
            subtitle = null,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.INFO,
            correctOptions = listOf(),
            distractors = listOf()
        )
        val exerciseToAdd2 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "New CLOZE lesson",
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