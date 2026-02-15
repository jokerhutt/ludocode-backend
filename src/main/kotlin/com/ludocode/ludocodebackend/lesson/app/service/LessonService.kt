package com.ludocode.ludocodebackend.lesson.app.service

import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.lesson.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.lesson.app.mapper.ExerciseMapper
import com.ludocode.ludocodebackend.lesson.app.mapper.LessonMapper
import com.ludocode.ludocodebackend.lesson.app.port.`in`.LessonPortForProgress
import com.ludocode.ludocodebackend.lesson.infra.projection.ExerciseFlatProjection
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonExercisesIdRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonExercisesRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.UserLessonProjection
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LessonService(
    private val exerciseMapper: ExerciseMapper,
    private val lessonExercisesRepository: LessonExercisesRepository,
    private val lessonMapper: LessonMapper,
    private val lessonRepository: LessonRepository
) : LessonPortForProgress {

    private val logger = LoggerFactory.getLogger(LessonService::class.java)

    override fun findLessonResponseById(lessonId: UUID, userId: UUID): LessonResponse {
        return lessonMapper.toLessonResponse(lessonRepository.findUserLesson(lessonId, userId) ?: throw ApiException(
            ErrorCode.LESSON_NOT_FOUND))
    }

    @Cacheable(CacheNames.LESSON_EXERCISES, key = "#lessonId")
    fun getExercisesByLessonId (lessonId: UUID): List<ExerciseResponse> {
        val exercisesWithOptionsFlat: List<ExerciseFlatProjection> = lessonExercisesRepository.getFlatExercisesWithOptions(lessonId)
        logger.info(
            LogEvents.LESSON_EXERCISES_LOADED + " {}",
            StructuredArguments.kv(LogFields.EXERCISE_COUNT, exercisesWithOptionsFlat.size)
        )
        return exerciseMapper.toLessonExercises(exercisesWithOptionsFlat)
    }

    @Cacheable(CacheNames.EXERCISE_SINGLE, key = "#exerciseId")
    fun getExerciseByExerciseId (exerciseId: UUID) : ExerciseResponse {
        val exerciseWithOptions = lessonExercisesRepository.getSingleExerciseNewestFlat(exerciseId)
        return exerciseMapper.toExerciseResponse(exerciseWithOptions)
    }

    internal fun getLessonsByIds (lessonIds: List<UUID>, userId: UUID): List<LessonResponse> {
        val lessons: List<UserLessonProjection> = lessonRepository.findUserLessonsByIds(lessonIds, userId)
        return lessonMapper.toLessonResponseList(lessons)
    }


}