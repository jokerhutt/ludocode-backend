package com.ludocode.ludocodebackend.lesson.app.service.admin

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForAI
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.lesson.app.service.LessonService
import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.lesson.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.lesson.infra.repository.ExerciseOptionRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.ExerciseRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonExercisesRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.OptionContentRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import java.util.*

@Service
class LessonSnapshotService(
    private val lessonExercisesRepository: LessonExercisesRepository,
    private val exerciseRepository: ExerciseRepository,
    private val optionContentRepository: OptionContentRepository,
    private val exerciseOptionRepository: ExerciseOptionRepository,
    private val lessonService: LessonService
) : CatalogPortForAI {


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

        return buildLessonCurriculumSnapshot(lessonId)

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

    fun buildLessonCurriculumSnapshot(lessonId: UUID): LessonCurriculumDraftSnapshot {

        val exerciseResponses = lessonService.getExercisesByLessonId(lessonId)
        val exerciseSnapshots = exerciseResponses.map { exerciseResponse ->
            buildExerciseSnapshot(exerciseResponse)
        }

        return LessonCurriculumDraftSnapshot(exerciseSnapshots)

    }


    internal fun buildExerciseSnapshot(exerciseResponse: ExerciseResponse): ExerciseSnap {
        return ExerciseSnap(
            id = exerciseResponse.id,
            title = exerciseResponse.title,
            subtitle = exerciseResponse.subtitle,
            prompt = exerciseResponse.prompt,
            exerciseType = exerciseResponse.exerciseType,
            media = exerciseResponse.exerciseMedia,
            correctOptions = exerciseResponse.correctOptions.map { opt ->
                OptionSnap(
                    content = opt.content,
                    answerOrder = opt.answerOrder,
                    exerciseOptionId = (opt.id)
                )
            },
            distractors = exerciseResponse.distractors.map { opt ->
                OptionSnap(
                    content = opt.content,
                    answerOrder = opt.answerOrder,
                    exerciseOptionId = opt.id
                )
            }
        )
    }

    override fun findExerciseSnapshotById(exerciseId: UUID): ExerciseSnap {
        val exerciseResponse = lessonService.getExerciseByExerciseId(exerciseId)
        return buildExerciseSnapshot(exerciseResponse)
    }


}