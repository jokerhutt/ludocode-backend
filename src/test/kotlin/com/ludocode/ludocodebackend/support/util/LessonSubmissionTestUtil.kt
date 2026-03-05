package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ClozeAnswer
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ClozeInteraction
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.domain.jsonb.SelectAnswer
import com.ludocode.ludocodebackend.lesson.domain.jsonb.SelectInteraction
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExerciseAnswer
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.support.TestRestClient
import java.util.UUID
import kotlin.random.Random

object LessonSubmissionTestUtil {

    fun completeLesson(
        userId: UUID,
        lessonSnap: LessonSnap,
        courseId: UUID,
        allCorrect: Boolean = true
    ): LessonCompletionPacket {

        val submissions = lessonSnap.exercises.map {
            createExerciseSubmission(it, allCorrect)
        }

        val request = LessonSubmissionRequest(
            submissionId = UUID.randomUUID(),
            lessonId = lessonSnap.id,
            courseId = courseId,
            exercises = submissions
        )

        return TestRestClient.postOk(
            ApiPaths.PROGRESS.COMPLETION.BASE,
            userId,
            request,
            LessonCompletionPacket::class.java
        )
    }

    fun buildExercises(
        lessonSnap: LessonSnap,
        allCorrect: Boolean = true
    ): List<ExerciseSubmissionRequest> =
        lessonSnap.exercises.map {
            createExerciseSubmission(it, allCorrect)
        }

    fun createRandomExerciseSubmission(
        exercise: ExerciseSnap,
        random: Random
    ): ExerciseSubmissionRequest =
        createExerciseSubmission(exercise, random.nextBoolean())

    private fun createExerciseSubmission(
        exercise: ExerciseSnap,
        allCorrect: Boolean
    ): ExerciseSubmissionRequest {

        val attempts: List<ExerciseAnswer> =
            when (val interaction = exercise.interaction) {

                // INFO exercise
                null -> listOf(
                    SelectAnswer("INFO")
                )

                is SelectInteraction -> {

                    if (allCorrect) {
                        listOf(
                            SelectAnswer(interaction.correctValue)
                        )
                    } else {
                        val wrong =
                            interaction.items.first { it != interaction.correctValue }

                        listOf(
                            SelectAnswer(wrong),
                            SelectAnswer(interaction.correctValue)
                        )
                    }
                }

                is ClozeInteraction -> {

                    val correctValues =
                        interaction.blanks.map { it.correctOptions.first() }

                    if (allCorrect) {
                        listOf(
                            ClozeAnswer(correctValues)
                        )
                    } else {

                        val wrongValues =
                            interaction.blanks.map { blank ->
                                interaction.options.first {
                                    it !in blank.correctOptions
                                }
                            }

                        listOf(
                            ClozeAnswer(wrongValues),
                            ClozeAnswer(correctValues)
                        )
                    }
                }
            }

        return ExerciseSubmissionRequest(
            exerciseId = exercise.exerciseId!!,
            version = 1,
            attempts = attempts
        )
    }
}