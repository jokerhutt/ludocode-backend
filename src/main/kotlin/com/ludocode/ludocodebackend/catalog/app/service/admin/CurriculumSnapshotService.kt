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
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseStatus
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseTagRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.lesson.domain.jsonb.HeaderBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ParagraphBlock
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.Lesson
import com.ludocode.ludocodebackend.lesson.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExecutableInteraction
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExecutableTest
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InstructionsBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.TestType
import com.ludocode.ludocodebackend.lesson.infra.repository.*
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
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
@Transactional
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
    private val languagesMapper: LanguagesMapper,
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
    fun applyCurriculumDiffs(courseId: UUID, snapshot: CurriculumDraftSnapshot): CurriculumDraftSnapshot {
        courseRepository.findById(courseId).orElseThrow()

        if (snapshot.modules.isEmpty()) {
            throw ApiException(ErrorCode.EMPTY_MODULES)
        }

        snapshot.modules.forEach {moduleSnap ->
            if (moduleSnap.lessons.isEmpty()) throw ApiException(ErrorCode.EMPTY_LESSONS)
        }

        deleteModulesInCourse(courseId)

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
                    isDeleted = false,
                    lessonType = lessonSnapshot.lessonType
                )

                if (isNewLesson) {
                        val newExercise = if (lesson.lessonType == LessonType.GUIDED) {exerciseRepository.save(
                            Exercise(
                                exerciseId = ExerciseId(UUID.randomUUID(), 1),
                                blocks = listOf(
                                    InstructionsBlock(clientId = UUID.randomUUID(), instructions = listOf("Make the program print hello world"))
                                ),
                                interaction = ExecutableInteraction(clientId = UUID.randomUUID(), tests = listOf(
                                    ExecutableTest(type = TestType.OUTPUT_CONTAINS, expected = "hello world")), solution = "CHANGE ME")
                            )
                        )} else {
                            exerciseRepository.save(
                                Exercise(
                                    exerciseId = ExerciseId(UUID.randomUUID(), 1),
                                    blocks = listOf(
                                        HeaderBlock("Placeholder Exercise"),
                                        ParagraphBlock("Change me")
                                    ),
                                    interaction = null
                                )
                            )
                        }

                    lessonExercisesRepository.save(
                        LessonExercise(
                            LessonExercisesId(lesson.id, 1),
                            newExercise.exerciseId.id,
                            newExercise.exerciseId.versionNumber
                        )
                    )

                    if (lesson.lessonType == LessonType.GUIDED) {

                        val courses = courseRepository.findAllWithLanguage()
                        val course = courses.find { it -> it.id == courseId } ?: throw ApiException(ErrorCode.COURSE_NOT_FOUND)
                        val language = course.language ?: throw ApiException(ErrorCode.LANGUAGE_NOT_FOUND, "Guided lessons course must have a language")

                        val initialFileId = UUID.randomUUID()

                        val initialFile = ProjectFileSnapshot(
                            id = initialFileId,
                            path = "${language.base}${language.base}",
                            language = languagesMapper.toLanguageMetadata(language),
                            content = ""
                        )

                        lesson.projectSnapshot = ProjectSnapshot(
                            projectId = UUID.randomUUID(),
                            projectName = "printing hello world",
                            projectLanguage = languagesMapper.toLanguageMetadata(language),
                            updatedAt = OffsetDateTime.now(),
                            deleteAt = null,
                            files = listOf(initialFile),
                            entryFileId = initialFileId
                        )
                    }

                }

                if (isNewLesson) {
                    courseProgressRepository.markCourseIncompleteForAllUsers(courseId, OffsetDateTime.now())
                }

                lesson.title = lessonSnapshot.title
                lesson.lessonType = lessonSnapshot.lessonType
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
            CacheEvict(cacheNames = [CacheNames.COURSE_LIST], allEntries = true),
        ]
    )
    @Transactional
    internal fun changeCourseStatus(courseId: UUID, value: CourseStatus) {

        val course = courseRepository.findById(courseId).orElseThrow { ApiException(ErrorCode.COURSE_NOT_FOUND) }
        if (course.courseStatus == value) return

        when (value) {
            CourseStatus.DRAFT -> throw ApiException(ErrorCode.NO_UNDRAFTING_COURSE)
            CourseStatus.PUBLISHED -> { course.courseStatus = value}
            CourseStatus.ARCHIVED -> {
                if (course.courseStatus == CourseStatus.DRAFT) {
                    throw ApiException(ErrorCode.NO_ARCHIVING_DRAFT_COURSE)
                }
                val publishedCourseCount = courseRepository.countByCourseStatusAndIsDeletedFalse(CourseStatus.PUBLISHED)
                if (course.courseStatus == CourseStatus.PUBLISHED && publishedCourseCount <= 1) {
                    throw ApiException(ErrorCode.NO_ALL_COURSES_INVISIBLE)
                }
                course.courseStatus = value
            }
        }
    }

    internal fun getAllCoursesAdminResponseList() : List<CourseResponse> {
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
    internal fun deleteCourse(courseId: UUID) {
        val courseToDelete = courseRepository.findById(courseId).orElseThrow { ApiException(ErrorCode.COURSE_NOT_FOUND) }

        if (courseToDelete.courseStatus != CourseStatus.DRAFT) {
            throw ApiException(ErrorCode.NO_DELETE_NON_DRAFT_COURSE)
        }

        deleteModulesInCourse(courseId)

        //TODO maybe just delete directly?
        courseToDelete.isDeleted = true
    }

    internal fun deleteModulesInCourse(courseId: UUID) {
        val courseModuleIds = moduleRepository.findActiveIdsByCourse(courseId)
        courseModuleIds.forEach { moduleId ->
            moduleLessonsRepository.deleteByModuleLessonsIdModuleId(moduleId)
        }

        moduleRepository.deleteByCourseId(courseId)
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
            description = newCourseDescription,
            courseStatus = CourseStatus.DRAFT
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
            lessonType = LessonType.NORMAL
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
                lessonType = lesson.lessonType,
            )
        }

        return ModuleDraftSnapshot(id = moduleId, title = module.title, lessons = lessonDraftSnapshots)
    }


}