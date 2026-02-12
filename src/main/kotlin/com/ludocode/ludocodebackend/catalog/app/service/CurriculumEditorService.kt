package com.ludocode.ludocodebackend.catalog.app.service
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLesson
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
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CurriculumEditorService(
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val moduleLessonsRepository: ModuleLessonsRepository,
    private val lessonRepository: LessonRepository,
    private val exerciseRepository: ExerciseRepository,
    private val lessonExercisesRepository: LessonExercisesRepository,
    private val exerciseOptionRepository: ExerciseOptionRepository,
    private val snapshotBuilderService: SnapshotBuilderService,
    private val optionContentRepository: OptionContentRepository
) {

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
                    val newExercise = exerciseRepository.save(Exercise(
                        exerciseId = ExerciseId(UUID.randomUUID(), 1),
                        title = "Placeholder Exercise",
                        prompt = "Change me",
                        exerciseType = ExerciseType.INFO,
                        isDeleted = false
                    ))
                    lessonExercisesRepository.save(LessonExercise(LessonExercisesId(lesson.id, 1), newExercise.exerciseId.id, newExercise.exerciseId.versionNumber))
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
            CacheEvict(cacheNames = [CacheNames.COURSE_TREE], key = "#courseId"),
            CacheEvict(cacheNames = [CacheNames.COURSE_FIRST_MODULE], key = "#courseId"),
            CacheEvict(cacheNames = [CacheNames.COURSE_LIST], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_EXERCISES], allEntries = true)
        ]
    )
    @Transactional
    fun applyExerciseDiffs (lessonId: UUID, lessonDraft: LessonCurriculumDraftSnapshot) : LessonCurriculumDraftSnapshot {

        val exercises = lessonDraft.exercises
        lessonExercisesRepository.deleteAllByLessonExercisesIdLessonId(lessonId)
        exerciseOptionRepository

        exercises.forEachIndexed { exerciseIndex, exercise ->

            val existing = exerciseRepository.findLatestActiveById(exercise.id)

            if (existing != null) {
                existing.isDeleted = true
            }

            val version = if (existing != null) existing.exerciseId.versionNumber + 1 else 1

            val exerciseEntity = exerciseRepository.save(Exercise(
                exerciseId = ExerciseId(exercise.id, versionNumber = version),
                title = exercise.title,
                subtitle = exercise.subtitle,
                prompt = exercise.prompt,
                exerciseType = exercise.exerciseType,
                exerciseMedia = exercise.media,
                isDeleted = false
            ))

            lessonExercisesRepository.save(LessonExercise(
                lessonExercisesId = LessonExercisesId(lessonId, exerciseIndex + 1),
                exerciseId = exerciseEntity.exerciseId.id,
                exerciseVersion = exerciseEntity.exerciseId.versionNumber
            ))

            val options = exercise.correctOptions + exercise.distractors
            applyOptionDiffs(options, exerciseEntity.exerciseId.id, exerciseEntity.exerciseId.versionNumber)

        }

        return snapshotBuilderService.buildLessonCurriculumSnapshot(lessonId)

    }

    fun applyOptionDiffs(options: List<OptionSnap>, exerciseId: UUID, exerciseVersion: Int) {

        for (option in options) {
            optionContentRepository.upsertOption(id = UUID.randomUUID(), option.content)
            val dbOption = optionContentRepository.findByContent(option.content)
            exerciseOptionRepository.save(ExerciseOption(
                id = UUID.randomUUID(),
                exerciseId = exerciseId,
                exerciseVersion = exerciseVersion,
                optionId = dbOption!!.id,
                answerOrder = option.answerOrder,
            ))


        }

    }

}