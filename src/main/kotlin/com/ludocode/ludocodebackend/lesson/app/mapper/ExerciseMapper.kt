package com.ludocode.ludocodebackend.lesson.app.mapper
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import org.springframework.stereotype.Component
@Component
class ExerciseMapper {

    fun toLessonExercises(exercises: List<Exercise>): List<ExerciseResponse> =
        exercises.mapIndexed { index, exercise ->
            ExerciseResponse(
                id = exercise.exerciseId.id,
                version = exercise.exerciseId.versionNumber,
                body = exercise.body,
                orderIndex = index + 1,
                blocks = exercise.blocks,
                interaction = exercise.interaction
            )
        }

    fun toExerciseResponse(
        exercise: Exercise,
        orderIndex: Int
    ): ExerciseResponse =
        ExerciseResponse(
            id = exercise.exerciseId.id,
            version = exercise.exerciseId.versionNumber,
            body = exercise.body,
            orderIndex = orderIndex,
            blocks = exercise.blocks,
            interaction = exercise.interaction
        )
}