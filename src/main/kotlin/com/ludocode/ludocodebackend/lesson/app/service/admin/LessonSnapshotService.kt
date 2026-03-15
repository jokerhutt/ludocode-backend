package com.ludocode.ludocodebackend.lesson.app.service.admin

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.lesson.app.service.LessonService
import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.lesson.infra.repository.ExerciseRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonExercisesRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import java.util.*

@Service
class LessonSnapshotService(
    private val lessonExercisesRepository: LessonExercisesRepository,
    private val lessonService: LessonService,
    private val exerciseRepository: ExerciseRepository,
    private val lessonRepository: LessonRepository
) {

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
    fun applyExercises(lessonId: UUID, snap: LessonCurriculumDraftSnapshot) : LessonCurriculumDraftSnapshot {

        val exerciseSnaps = snap.exercises

        if (exerciseSnaps.isEmpty()) throw ApiException(ErrorCode.EMPTY_EXERCISES)
        lessonExercisesRepository.deleteAllByLessonExercisesIdLessonId(lessonId)

        exerciseSnaps.forEachIndexed { exerciseIndex, exercise ->

            val exerciseId = exercise.exerciseId ?: UUID.randomUUID()

            val existing =
                exerciseRepository
                    .findTopByExerciseId_IdAndIsDeletedFalseOrderByExerciseId_VersionNumberDesc(exerciseId)

            val version = if (existing != null) existing.exerciseId.versionNumber + 1 else 1

            if (existing != null) {
                existing.isDeleted = true
            }

            val exerciseEntity = exerciseRepository.save(
                Exercise(
                    ExerciseId(exerciseId, version),
                    blocks = exercise.blocks,
                    body = exercise.body,
                    interaction = exercise.interaction,
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
        }

        val lesson = lessonRepository.findById(lessonId).orElseThrow { ApiException(ErrorCode.LESSON_NOT_FOUND) }
        lesson.projectSnapshot = snap.projectSnapshot

        return buildLessonCurriculumSnapshot(lessonId)
    }

    fun buildLessonCurriculumSnapshot(lessonId: UUID): LessonCurriculumDraftSnapshot {
        val lesson = lessonRepository.findById(lessonId).orElseThrow { ApiException(ErrorCode.LESSON_NOT_FOUND) }
        val exerciseResponses = lessonService.getExercisesByLessonId(lessonId)
        val exerciseSnapshots = exerciseResponses.map { exerciseResponse ->
            buildExerciseSnapshot(exerciseResponse)
        }

        return LessonCurriculumDraftSnapshot(exerciseSnapshots, lessonType = lesson.lessonType,  projectSnapshot = lesson.projectSnapshot)

    }


    internal fun buildExerciseSnapshot(
        exerciseResponse: ExerciseResponse
    ): ExerciseSnap {

        return ExerciseSnap(
            exerciseId = exerciseResponse.id,
            blocks = exerciseResponse.blocks,
            interaction = exerciseResponse.interaction
        )
    }

}