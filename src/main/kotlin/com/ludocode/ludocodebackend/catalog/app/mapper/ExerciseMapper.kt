package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseOptionResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.catalog.infra.projection.ExerciseFlatProjection
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import org.springframework.stereotype.Component

@Component
class ExerciseMapper(private val basicMapper: BasicMapper) {

    fun toLessonExercises(rows: List<ExerciseFlatProjection>): List<ExerciseResponse> =
        rows.groupBy { it.getExerciseId() }
            .values
            .map { group -> toExerciseResponse(group) }

    private fun toExerciseOptionResponse(p: ExerciseFlatProjection) =
        ExerciseOptionResponse(
            id = requireNotNull(p.getOptionId()),
            content = p.getContent().orEmpty(),
            answerOrder = p.getAnswerOrder(),
            exerciseVersion = p.getVersion()
        )

    private fun toExerciseResponse(group: List<ExerciseFlatProjection>): ExerciseResponse {
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
            distractors = distractors
        )
    }




}