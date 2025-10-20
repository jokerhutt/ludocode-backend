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

    private fun toExerciseResponse(group: List<ExerciseFlatProjection>): ExerciseResponse {
        val h = group.first()
        return ExerciseResponse(
            id = h.getExerciseId(),
            title = h.getTitle(),
            prompt = h.getPrompt(),
            exerciseType = ExerciseType.valueOf(h.getExerciseType()),
            lessonId = h.getLessonId(),
            exerciseOptions = group
                .filter { it.getOptionId() != null }
                .map { toExerciseOptionResponse(it) }
        )
    }

    private fun toExerciseOptionResponse(row: ExerciseFlatProjection): ExerciseOptionResponse =
        ExerciseOptionResponse(
            id = row.getOptionId()!!,
            content = row.getContent()!!,
            answerOrder = row.getAnswerOrder()
        )




}