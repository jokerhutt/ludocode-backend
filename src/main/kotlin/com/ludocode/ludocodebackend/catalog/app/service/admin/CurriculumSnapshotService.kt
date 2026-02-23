package com.ludocode.ludocodebackend.catalog.app.service.admin

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Subject
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.SubjectRepository
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.lesson.app.service.admin.LessonSnapshotService
import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.lesson.domain.entity.Lesson
import com.ludocode.ludocodebackend.lesson.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.lesson.infra.repository.*
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class CurriculumSnapshotService(
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val moduleLessonsRepository: ModuleLessonsRepository,
    private val lessonSnapshotService: LessonSnapshotService,
    private val exerciseRepository: ExerciseRepository,
    private val lessonExercisesRepository: LessonExercisesRepository,
    private val courseMapper: CourseMapper,
    private val lessonRepository: LessonRepository,
    private val courseProgressRepository: CourseProgressRepository,
    private val optionContentRepository: OptionContentRepository,
    private val exerciseOptionRepository: ExerciseOptionRepository,
    private val codeLanguagesRepository: CodeLanguagesRepository,
    private val subjectRepository: SubjectRepository
) {

    private val logger = LoggerFactory.getLogger(CurriculumSnapshotService::class.java)

    @Caching(
        evict = [
            CacheEvict(cacheNames = [CacheNames.COURSE_TREE], key = "#courseId"),
            CacheEvict(cacheNames = [CacheNames.COURSE_FIRST_MODULE], key = "#courseId"),
            CacheEvict(cacheNames = [CacheNames.COURSE_LIST], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_EXERCISES], allEntries = true)
        ]
    )
    @Transactional
    fun applyCurriculumDiffs(courseId: UUID, snapshot: CurriculumDraftSnapshot): CurriculumDraftSnapshot {
        courseRepository.findById(courseId).orElseThrow()

        if (snapshot.modules.isEmpty()) {
            throw ApiException(ErrorCode.EMPTY_MODULES)
        }

        snapshot.modules.forEach {moduleSnap ->
            if (moduleSnap.lessons.isEmpty()) throw ApiException(ErrorCode.EMPTY_LESSONS)
        }

        val oldModuleIds = moduleRepository.findActiveIdsByCourse(courseId)
        oldModuleIds.forEach { moduleId ->
            moduleLessonsRepository.deleteByModuleLessonsIdModuleId(moduleId)
        }

        moduleRepository.deleteByCourseId(courseId)

        snapshot.modules.forEachIndexed { moduleIndex, moduleSnapshot ->

            val module = Module(
                id = moduleSnapshot.id,
                title = moduleSnapshot.title,
                courseId = courseId,
                orderIndex = moduleIndex + 1,
                isDeleted = false
            )
            moduleRepository.save(module)

            moduleSnapshot.lessons.forEachIndexed { lessonIndex, lessonSnapshot ->

                val existing = lessonRepository.findActiveById(lessonSnapshot.id)

                val isNewLesson = existing == null

                val lesson = existing ?: Lesson(
                    id = lessonSnapshot.id,
                    title = lessonSnapshot.title,
                    isDeleted = false
                )

                if (isNewLesson) {
                    val newExercise = exerciseRepository.save(
                        Exercise(
                            exerciseId = ExerciseId(UUID.randomUUID(), 1),
                            title = "Placeholder Exercise",
                            prompt = "Change me",
                            exerciseType = ExerciseType.INFO,
                            isDeleted = false
                        )
                    )
                    lessonExercisesRepository.save(
                        LessonExercise(
                            LessonExercisesId(lesson.id, 1),
                            newExercise.exerciseId.id,
                            newExercise.exerciseId.versionNumber
                        )
                    )
                }

                if (isNewLesson) {
                    courseProgressRepository.markCourseIncompleteForAllUsers(courseId, OffsetDateTime.now())
                }

                lesson.title = lessonSnapshot.title
                lessonRepository.save(lesson)

                val join = ModuleLesson(
                    moduleLessonsId = ModuleLessonsId(
                        moduleId = module.id,
                        orderIndex = lessonIndex + 1
                    ),
                    lessonId = lessonSnapshot.id
                )
                moduleLessonsRepository.save(join)
            }
        }

        return snapshot
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [CacheNames.COURSE_TREE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.COURSE_FIRST_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.COURSE_LIST], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_EXERCISES], allEntries = true)
        ]
    )
    @Transactional
    fun applyExerciseDiffs(lessonId: UUID, lessonDraft: LessonCurriculumDraftSnapshot): LessonCurriculumDraftSnapshot {

        val exercises = lessonDraft.exercises
        lessonExercisesRepository.deleteAllByLessonExercisesIdLessonId(lessonId)

        exercises.forEachIndexed { exerciseIndex, exercise ->

            val existing = exerciseRepository.findLatestActiveById(exercise.id)

            if (existing != null) {
                existing.isDeleted = true
            }

            val version = if (existing != null) existing.exerciseId.versionNumber + 1 else 1

            val exerciseEntity = exerciseRepository.save(
                Exercise(
                    exerciseId = ExerciseId(exercise.id, versionNumber = version),
                    title = exercise.title,
                    subtitle = exercise.subtitle,
                    prompt = exercise.prompt,
                    exerciseType = exercise.exerciseType,
                    exerciseMedia = exercise.media,
                    isDeleted = false
                )
            )

            lessonExercisesRepository.save(
                LessonExercise(
                    lessonExercisesId = LessonExercisesId(lessonId, exerciseIndex + 1),
                    exerciseId = exerciseEntity.exerciseId.id,
                    exerciseVersion = exerciseEntity.exerciseId.versionNumber
                )
            )

            val options = exercise.correctOptions + exercise.distractors
            applyOptionDiffs(options, exerciseEntity.exerciseId.id, exerciseEntity.exerciseId.versionNumber)

        }

        return lessonSnapshotService.buildLessonCurriculumSnapshot(lessonId)

    }

    fun applyOptionDiffs(options: List<OptionSnap>, exerciseId: UUID, exerciseVersion: Int) {

        for (option in options) {
            optionContentRepository.upsertOption(id = UUID.randomUUID(), option.content)
            val dbOption = optionContentRepository.findByContent(option.content)
            exerciseOptionRepository.save(
                ExerciseOption(
                    id = UUID.randomUUID(),
                    exerciseId = exerciseId,
                    exerciseVersion = exerciseVersion,
                    optionId = dbOption!!.id,
                    answerOrder = option.answerOrder,
                )
            )


        }

    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [CacheNames.COURSE_TREE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.COURSE_FIRST_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.COURSE_LIST], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_EXERCISES], allEntries = true)
        ]
    )
    @Transactional
    internal fun createCourse(request: CreateCourseRequest): List<CourseResponse> {
        val newCourseName = request.courseTitle
        val newCourseHash = request.requestHash
        val newCourseType = request.courseType
        val newCourseSubjectId = request.courseSubjectId

        val newCourseId = UUID.randomUUID()
        val newModuleId = UUID.randomUUID()
        val newLessonId = UUID.randomUUID()
        val newExerciseId = UUID.randomUUID()

        val codeLanguage =
            request.languageId?.let { id ->
                codeLanguagesRepository.findByIdOrNull(id)
                    ?: throw ApiException(ErrorCode.LANGUAGE_NOT_FOUND)
            }

        val subject = subjectRepository.findById(newCourseSubjectId)
            .orElseThrow { ApiException(ErrorCode.SUBJECT_NOT_FOUND) }

        val newCourse = Course(
            id = newCourseId,
            title = newCourseName,
            requestHash = newCourseHash,
            courseType = newCourseType,
            subject = subject,
            language = codeLanguage

        )

        courseRepository.save(newCourse)

        val newModuleTitle = "Intro to $newCourseName"

        val newModule = Module(
            id = newModuleId,
            title = newModuleTitle,
            isDeleted = false,
            orderIndex = 1,
            courseId = newCourseId
        )
        moduleRepository.save(newModule)

        val newLesson = Lesson(
            id = newLessonId,
            title = "Hello world!",
            isDeleted = false,
        )

        lessonRepository.save(newLesson)

        val newModuleLesson = ModuleLesson(
            moduleLessonsId = ModuleLessonsId(newModuleId, 1),
            lessonId = newLessonId
        )

        moduleLessonsRepository.save(newModuleLesson)

        val newExerciseTitle = "Welcome to $newCourseName"

        val newExercise = Exercise(
            exerciseId = ExerciseId(newExerciseId, 1),
            title = newExerciseTitle,
            prompt = null,
            subtitle = null,
            exerciseType = ExerciseType.INFO,
            exerciseMedia = null,
            isDeleted = false
        )

        exerciseRepository.save(newExercise)

        val newLessonExercise = LessonExercise(
            lessonExercisesId = LessonExercisesId(newLessonId, 1),
            exerciseId = newExerciseId,
            exerciseVersion = newExercise.exerciseId.versionNumber
        )

        lessonExercisesRepository.save(newLessonExercise)

        logger.info(
            LogEvents.COURSE_CREATED + " {} {} {} {}",
            kv(LogFields.COURSE_ID, newCourseId.toString()),
            kv(LogFields.MODULE_COUNT, 1),
            kv(LogFields.LESSON_COUNT, 1),
            kv(LogFields.EXERCISE_COUNT, 1)
        )

        return courseMapper.toCourseResponseList(courseRepository.findAll())

    }

    fun buildCurriculumSnapshot(courseId: UUID): CurriculumDraftSnapshot {

        val moduleIds = moduleRepository.findActiveIdsByCourse(courseId)
        val modules = moduleRepository.findAllByIdIn(moduleIds)

        val moduleDraftSnapshots = modules.map { module ->
            buildModuleDraftSnapshot(module)
        }

        return CurriculumDraftSnapshot(modules = moduleDraftSnapshots)

    }

    private fun buildModuleDraftSnapshot(module: Module): ModuleDraftSnapshot {

        val moduleId = module.id
        val lessons = moduleLessonsRepository.findActiveLessonsByModuleId(moduleId)

        val lessonDraftSnapshots = lessons.map { lesson ->
            LessonDraftSnapshot(
                id = lesson.id,
                title = lesson.title,
            )
        }

        return ModuleDraftSnapshot(id = moduleId, title = module.title, lessons = lessonDraftSnapshots)
    }


}