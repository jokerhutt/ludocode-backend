package com.ludocode.ludocodebackend.catalog.app.service.admin

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLesson
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseTagRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.lesson.domain.jsonb.HeaderBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ParagraphBlock
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.Lesson
import com.ludocode.ludocodebackend.lesson.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.lesson.infra.repository.*
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import com.ludocode.ludocodebackend.tag.api.dto.TagMetadata
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
    private val exerciseRepository: ExerciseRepository,
    private val lessonExercisesRepository: LessonExercisesRepository,
    private val courseMapper: CourseMapper,
    private val lessonRepository: LessonRepository,
    private val courseProgressRepository: CourseProgressRepository,
    private val codeLanguagesRepository: CodeLanguagesRepository,
    private val courseTagRepository: CourseTagRepository,
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
                            blocks = listOf(
                                HeaderBlock("Placeholder Exercise"),
                                ParagraphBlock("Change me")
                            ),
                            interaction = null
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

    internal fun createCourseReturningList(request: CreateCourseRequest): List<CourseResponse> {
        createCourse(request)
        val courseTags = courseTagRepository.getAllCourseTags()

        val tagsByCourse =
            courseTags.groupBy({ it.courseId }) {
                TagMetadata(it.id, it.name, it.slug)
            }
        return courseMapper.toCourseResponseList(courseRepository.findAll(), tagsByCourse)
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
    internal fun createCourse(request: CreateCourseRequest): UUID {
        val newCourseName = request.courseTitle
        val newCourseHash = request.requestHash
        val newCourseType = request.courseType
        val newCourseDescription = request.description ?: "No description"
        val newCourseIcon = request.courseIcon

        val newCourseId = UUID.randomUUID()
        val newModuleId = UUID.randomUUID()
        val newLessonId = UUID.randomUUID()
        val newExerciseId = UUID.randomUUID()


        val codeLanguage =
            request.languageId?.let { id ->
                codeLanguagesRepository.findByIdOrNull(id)
                    ?: throw ApiException(ErrorCode.LANGUAGE_NOT_FOUND)
            }

        val newCourse = Course(
            id = newCourseId,
            title = newCourseName,
            requestHash = newCourseHash,
            courseType = newCourseType,
            language = codeLanguage,
            courseIcon = newCourseIcon,
            description = newCourseDescription
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

        val newExercise = Exercise(
            exerciseId = ExerciseId(newExerciseId, 1),
            blocks = listOf(
                HeaderBlock("Welcome to $newCourseName"),
                ParagraphBlock("Let's start your journey.")
            ),
            interaction = null
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

        return newCourse.id

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