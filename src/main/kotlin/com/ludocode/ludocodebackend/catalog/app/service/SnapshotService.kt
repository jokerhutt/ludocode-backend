package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnap
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Subject
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseOptionRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonExercisesRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.OptionContentRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.SubjectRepository
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.playground.infra.repository.CodeLanguagesRepository
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class SnapshotService(
    private val lessonRepository: LessonRepository,
    private val moduleLessonsRepository: ModuleLessonsRepository,
    private val lessonExercisesRepository: LessonExercisesRepository,
    private val exerciseRepository: ExerciseRepository,
    private val optionContentRepository: OptionContentRepository,
    private val moduleRepository: ModuleRepository,
    private val exerciseOptionRepository: ExerciseOptionRepository,
    private val snapshotBuilderService: SnapshotBuilderService,
    private val em: EntityManager,
    private val courseMapper: CourseMapper,
    private val courseRepository: CourseRepository,
    private val subjectRepository: SubjectRepository,
    private val codeLanguagesRepository: CodeLanguagesRepository
) {

    private val logger = LoggerFactory.getLogger(SnapshotService::class.java)

    @Transactional
    fun applyNewSnapshot (reqSnapshot: CourseSnap): CourseSnap? {
        return applyModuleDiffs(reqSnapshot)
    }

    @Transactional
    internal fun createCourse (request: CreateCourseRequest) : List<CourseResponse> {
        val newCourseName = request.courseTitle
        val newCourseHash = request.requestHash
        val newCourseSubject = request.courseSubject
        val newCourseType = request.courseType

        val newCourseId = UUID.randomUUID()
        val newModuleId = UUID.randomUUID()
        val newLessonId = UUID.randomUUID()
        val newExerciseId = UUID.randomUUID()

        val codeLanguage = codeLanguagesRepository.findById(request.courseSubject.codeLanguageId)
            .orElseThrow { ApiException(ErrorCode.LANGUAGE_NOT_FOUND) }

        val subject =
            subjectRepository.findBySlugAndName(newCourseSubject.slug, newCourseSubject.name)
                ?: subjectRepository.save(
                    Subject(
                        slug = newCourseSubject.slug,
                        name = newCourseSubject.name,
                        codeLanguage = codeLanguage
                    )
                )

        val newCourse = Course(
            id = newCourseId,
            title = newCourseName,
            requestHash = newCourseHash,
            courseType = newCourseType,
            subject = subject,

        )

        courseRepository.save(newCourse)

        val newModuleTitle = "Intro to $newCourseName"

        val newModule = Module (
            id = newModuleId,
            title = newModuleTitle,
            isDeleted = false,
            orderIndex = 1,
            courseId = newCourseId
        )
        moduleRepository.save(newModule)

        val newLesson = Lesson (
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

        val newExercise = Exercise (
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

    fun getCourseSnapshot (courseId: UUID): CourseSnap {
        return snapshotBuilderService.buildCourseSnapshot(courseId)
    }

    private fun applyLessonDiffs(reqModuleSnap: ModuleSnap) {

        val moduleId = reqModuleSnap.moduleId
        val submittedLessonDiffs = reqModuleSnap.lessons
        val activeLessonIdsInModule = moduleLessonsRepository.findActiveLessonIdsByModuleId(moduleId)
        val submittedLessonDiffsIds = submittedLessonDiffs.map { it.id }
        val lessonsToDelete: List<UUID> = getIdsToDelete(submittedLessonDiffsIds, activeLessonIdsInModule)

        for (lessonId in lessonsToDelete) {
            lessonRepository.softDeleteLessonById(lessonId)
        }

        for (i in 0 until submittedLessonDiffs.size) {
            val submittedDiff = submittedLessonDiffs[i]
            val existing : Lesson? = lessonRepository.findActiveById(submittedDiff.id)
            if (existing == null) {
                val newLesson = Lesson(
                    id = submittedDiff.id,
                    title = submittedDiff.title,
                    isDeleted = false
                )
                lessonRepository.save(newLesson)
            } else {
                existing.title = submittedDiff.title
                lessonRepository.save(existing)
            }
        }
        moduleLessonsRepository.deleteLessonsInModule(moduleId)
        em.flush()
        em.clear()

        for (i in 0 until submittedLessonDiffs.size) {
            val newOrderIndex = i + 1
            val lessonId = submittedLessonDiffs[i].id
            val moduleLesson = ModuleLesson(
                moduleLessonsId = ModuleLessonsId(
                    moduleId = moduleId,
                    orderIndex = newOrderIndex
                ),
                lessonId = lessonId
            )
            val dbLesson = moduleLessonsRepository.save(moduleLesson)
            applyExerciseDiffs(dbLesson.lessonId, submittedLessonDiffs[i].exercises)
        }

    }

    private fun applyExerciseDiffs(lessonId: UUID, submittedExerciseDiffs: List<ExerciseSnap>) {

        val activeExerciseIdsInLesson = lessonExercisesRepository.findActiveExercisesByLessonId(lessonId)
        val submittedExerciseDiffIds = submittedExerciseDiffs.map { it.id }
        val exercisesToDelete = getIdsToDelete(submittedExerciseDiffIds, activeExerciseIdsInLesson)

        for (exerciseId in exercisesToDelete) {
            exerciseRepository.softDeleteExerciseById(exerciseId)
        }

        for (i in 0 until submittedExerciseDiffs.size) {
            val submittedExerciseDiff = submittedExerciseDiffs[i]
            val existing = exerciseRepository.findLatestActiveById(submittedExerciseDiff.id)
            val newVersion = if (existing != null) existing.exerciseId.versionNumber + 1 else 1

            val newExercise = Exercise(
                exerciseId = ExerciseId(submittedExerciseDiff.id, newVersion),
                title = submittedExerciseDiff.title,
                subtitle = submittedExerciseDiff.subtitle,
                prompt = submittedExerciseDiff.prompt,
                exerciseMedia = submittedExerciseDiff.media,
                exerciseType = submittedExerciseDiff.exerciseType,
                isDeleted = false
            )
            exerciseRepository.save(newExercise)
        }

        lessonExercisesRepository.deleteExerciseInLesson(lessonId)
        em.flush()
        em.clear()

        for (i in 0 until submittedExerciseDiffs.size) {
            val newOrderIndex = i + 1
            val exerciseId = submittedExerciseDiffs[i].id
            val existingExercise = exerciseRepository.findLatestActiveById(exerciseId)

            val newLessonExercise = LessonExercise(
                lessonExercisesId = LessonExercisesId(lessonId = lessonId, orderIndex = newOrderIndex),
                exerciseId = existingExercise!!.exerciseId.id,
                exerciseVersion = existingExercise!!.exerciseId.versionNumber
            )
            val dbExercise = lessonExercisesRepository.save(newLessonExercise)
            val dbExerciseId = dbExercise.exerciseId
            val dbExerciseVersion = dbExercise.exerciseVersion
            applyExerciseOptionDiffs(submittedExerciseDiffs[i], dbExerciseId, dbExerciseVersion)
        }

    }

    private fun applyExerciseOptionDiffs(reqExerciseSnapshot: ExerciseSnap,  dbExerciseId: UUID, dbExerciseVersion: Int) {

        val allOptions = reqExerciseSnapshot.correctOptions + reqExerciseSnapshot.distractors

        for (i in 0 until allOptions.size) {
            val option = allOptions[i]
            optionContentRepository.upsertOption(id = UUID.randomUUID(), option.content)
            val dbOption = optionContentRepository.findOptionContentByContent(option.content)
            val newExerciseOption = ExerciseOption(
                id = UUID.randomUUID(),
                exerciseId = dbExerciseId,
                exerciseVersion = dbExerciseVersion,
                answerOrder = option.answerOrder,
                optionId = dbOption!!.id
            )
            exerciseOptionRepository.save(newExerciseOption)
        }
    }

    //TODO clean this up with delay
    private fun applyModuleDiffs(reqSnapshot: CourseSnap): CourseSnap {

        val courseId : UUID = reqSnapshot.courseId
        val submittedModuleDiffs : List<ModuleSnap> = reqSnapshot.modules
        val activeModuleIds : List<UUID> = moduleRepository.findActiveIdsByCourse(courseId = courseId)

        val submittedModuleDiffsIds = submittedModuleDiffs.map { it.moduleId }
        val modulesToDelete : List<UUID> = getIdsToDelete(submittedModuleDiffsIds, activeModuleIds)

        logger.info(
            LogEvents.COURSE_SNAPSHOT_APPLY + " {} {} {}",
            kv(LogFields.COURSE_ID, courseId.toString()),
            kv(LogFields.MODULE_COUNT, submittedModuleDiffs.size),
            kv(LogFields.DELETE_COUNT, modulesToDelete.size),
        )

        for (moduleId in modulesToDelete) {
            moduleRepository.softDeleteModulesByModuleId(moduleId)
        }

        moduleRepository.bumpAllModuleOrderIndexesInCourse(courseId)
        em.flush()
        em.clear()


        for (i in 0 until submittedModuleDiffs.size) {

            val newOrderIndex = i + 1
            val moduleDiff = submittedModuleDiffs[i]
            val existing: Module? = moduleRepository.findActiveById(moduleDiff.moduleId)

            if (existing == null) {
                val newModule = Module(
                    id = moduleDiff.moduleId,
                    title = moduleDiff.title,
                    isDeleted = false,
                    courseId = courseId,
                    orderIndex = newOrderIndex
                )
                moduleRepository.save(newModule)
            } else {
                existing.title = moduleDiff.title
                existing.orderIndex = newOrderIndex
                moduleRepository.save(existing)
            }

            applyLessonDiffs(submittedModuleDiffs[i])

        }

        return snapshotBuilderService.buildCourseSnapshot(courseId)

    }

    private fun getIdsToDelete(
        newIds: List<UUID?>,
        existingIds: List<UUID>
    ): List<UUID> {
        val incoming = newIds.filterNotNull().toSet()
        return existingIds.filterNot { it in incoming }
    }

}