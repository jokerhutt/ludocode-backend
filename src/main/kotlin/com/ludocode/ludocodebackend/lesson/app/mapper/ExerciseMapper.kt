package com.ludocode.ludocodebackend.lesson.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseOptionResponse
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.lesson.infra.projection.ExerciseFlatProjection
import org.springframework.stereotype.Component

@Component
class ExerciseMapper(private val basicMapper: BasicMapper) {

    fun toLessonExercises(rows: List<ExerciseFlatProjection>): List<ExerciseResponse> =
        rows.groupBy { it.getExerciseId() }
            .values
            .map { group -> toExerciseResponse(group) }

    fun toExerciseResponse(rows: List<ExerciseFlatProjection>): ExerciseResponse =
        toExerciseResponseInternal(rows)

    private fun toExerciseOptionResponse(p: ExerciseFlatProjection) =
        ExerciseOptionResponse(
            id = requireNotNull(p.getOptionId()),
            content = p.getContent().orEmpty(),
            answerOrder = p.getAnswerOrder(),
            exerciseVersion = p.getVersion()
        )

    private fun toExerciseResponseInternal(group: List<ExerciseFlatProjection>): ExerciseResponse {
        val h = group.first()
        val order = h.getOrderIndex() ?: 1

        val optionRows = group.filter { it.getOptionId() != null }
        val (correctRows, distractorRows) = optionRows.partition { it.getAnswerOrder() != null }

        val correctOptions = correctRows
            .sortedBy { it.getAnswerOrder() }
            .map(::toExerciseOptionResponse)

        val distractors = distractorRows
            .map(::toExerciseOptionResponse)

        return ExerciseResponse(
            id = h.getExerciseId(),
            title = h.getTitle(),
            prompt = h.getPrompt(),
            exerciseType = ExerciseType.valueOf(h.getExerciseType()),
            lessonId = h.getLessonId(),
            version = h.getVersion(),
            orderIndex = order,
            subtitle = h.getSubtitle(),
            correctOptions = correctOptions,
            distractors = distractors,
            exerciseMedia = h.getExerciseMedia()
        )
    }
}