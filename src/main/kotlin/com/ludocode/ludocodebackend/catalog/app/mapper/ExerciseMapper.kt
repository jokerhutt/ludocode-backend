package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import org.springframework.stereotype.Component

@Component
class ExerciseMapper(private val basicMapper: BasicMapper) {

    fun toExerciseResponse(exercise: Exercise): ExerciseResponse =
        basicMapper.one(exercise) {
            ExerciseResponse(
                id = it.id!!,
                title = it.title!!,
                prompt = it.prompt!!,
                exerciseType = it.exerciseType!!,
                lessonId = it.lessonId!!
            )
        }

    fun toExerciseResponseList(exercise: List<Exercise>): List<ExerciseResponse> =
        basicMapper.list(exercise) { exercise ->
            toExerciseResponse(exercise)
        }


}