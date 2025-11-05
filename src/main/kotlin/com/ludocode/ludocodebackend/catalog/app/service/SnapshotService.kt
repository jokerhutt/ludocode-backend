package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.LessonExercises
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLessons
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseOptionRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonExercisesRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.OptionContentRepository
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
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
    private val em: EntityManager
) {

    @Transactional
    fun applyNewSnapshot (reqSnapshot: CourseSnap): CourseSnap? {
        return applyModuleDiffs(reqSnapshot)
    }

    fun getCourseSnapshot (courseId: UUID): CourseSnap {
        return snapshotBuilderService.buildCourseSnapshot(courseId)
    }

    @Transactional
    fun applyLessonDiffs(reqModuleSnapshot: ModuleSnapshot) {

        val moduleId = reqModuleSnapshot.moduleId
        val submittedLessonDiffs = reqModuleSnapshot.lessons
        val activeLessonIdsInModule = moduleLessonsRepository.findActiveLessonIdsByModuleId(moduleId)
        val submittedLessonDiffsIds = submittedLessonDiffs.map { it.id }
        val lessonsToDelete: List<UUID> = getIdsToDelete(submittedLessonDiffsIds, activeLessonIdsInModule)

        System.out.println("Deleting Lessons")
        for (lessonId in lessonsToDelete) {
            lessonRepository.softDeleteLessonById(lessonId)
        }
        System.out.println("Deleted Lessons")

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

        System.out.println("deleting Lessons i nmodule")


        moduleLessonsRepository.deleteLessonsInModule(moduleId)
        System.out.println("deleted Lessons i nmodule")

        em.flush()
        em.clear()

        for (i in 0 until submittedLessonDiffs.size) {
            val newOrderIndex = i + 1
            val lessonId = submittedLessonDiffs[i].id
            val moduleLesson = ModuleLessons(
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

    @Transactional
    fun applyExerciseDiffs(lessonId: UUID, submittedExerciseDiffs: List<ExerciseSnap>) {

        val activeExerciseIdsInLesson = lessonExercisesRepository.findActiveExercisesByLessonId(lessonId)
        val submittedExerciseDiffIds = submittedExerciseDiffs.map { it.id }
        val exercisesToDelete = getIdsToDelete(submittedExerciseDiffIds, activeExerciseIdsInLesson)

        for (exerciseId in exercisesToDelete) {
            exerciseRepository.softDeleteExerciseById(exerciseId)
        }

        for (i in 0 until submittedExerciseDiffs.size) {
            val submittedExerciseDiff = submittedExerciseDiffs[i]
            val existing = exerciseRepository.findLatestActiveById(submittedExerciseDiff.id)
            val newVersion = if (existing != null) existing.exerciseId.version + 1 else 1

            val newExercise = Exercise(
                exerciseId = ExerciseId(submittedExerciseDiff.id, newVersion),
                title = submittedExerciseDiff.title,
                subtitle = submittedExerciseDiff.subtitle,
                prompt = submittedExerciseDiff.prompt,
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

            val newLessonExercise = LessonExercises(
                lessonExercisesId = LessonExercisesId(lessonId = lessonId, orderIndex = newOrderIndex),
                exerciseId = existingExercise!!.exerciseId.id,
                exerciseVersion = existingExercise!!.exerciseId.version
            )
            val dbExercise = lessonExercisesRepository.save(newLessonExercise)
            val dbExerciseId = dbExercise.exerciseId
            val dbExerciseVersion = dbExercise.exerciseVersion
            applyExerciseOptionDiffs(submittedExerciseDiffs[i], dbExerciseId, dbExerciseVersion)
        }

    }

    @Transactional
    fun applyExerciseOptionDiffs(reqExerciseSnapshot: ExerciseSnap,  dbExerciseId: UUID, dbExerciseVersion: Int) {

        val allOptions = reqExerciseSnapshot.correctOptions + reqExerciseSnapshot.distractors

        for (i in 0 until allOptions.size) {
            val option = allOptions[i]
            optionContentRepository.upsertOption(option.content)
            val dbOption = optionContentRepository.findOptionContentByContent(option.content)
            val newExerciseOption = ExerciseOption(
                id = UUID.randomUUID(),
                exerciseId = dbExerciseId,
                exerciseVersion = dbExerciseVersion,
                optionId = dbOption!!.id
            )
            exerciseOptionRepository.save(newExerciseOption)
        }
    }

    @Transactional
    fun applyModuleDiffs(reqSnapshot: CourseSnap): CourseSnap {

        val courseId : UUID = reqSnapshot.courseId
        val submittedModuleDiffs : List<ModuleSnapshot> = reqSnapshot.modules
        val activeModuleIds : List<UUID> = moduleRepository.findActiveIdsByCourse(courseId = courseId)

        val submittedModuleDiffsIds = submittedModuleDiffs.map { it.moduleId }
        val modulesToDelete : List<UUID> = getIdsToDelete(submittedModuleDiffsIds, activeModuleIds)

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