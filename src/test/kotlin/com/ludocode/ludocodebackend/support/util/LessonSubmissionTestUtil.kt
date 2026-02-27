package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.exercise.ClozeAnswer
import com.ludocode.ludocodebackend.exercise.ClozeInteraction
import com.ludocode.ludocodebackend.exercise.LExercise
import com.ludocode.ludocodebackend.exercise.MCQAnswer
import com.ludocode.ludocodebackend.exercise.SelectInteraction
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
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
        exercise: LExercise,
        random: Random
    ): ExerciseSubmissionRequest =
        createExerciseSubmission(exercise, random.nextBoolean())

    private fun createExerciseSubmission(
        exercise: LExercise,
        allCorrect: Boolean
    ): ExerciseSubmissionRequest {

        val attempts: List<ExerciseAttemptRequest> =
            when (val interaction = exercise.interaction) {

                // INFO exercise
                null -> listOf(
                    ExerciseAttemptRequest(
                        answer = MCQAnswer("INFO")
                    )
                )

                is SelectInteraction -> {

                    if (allCorrect) {
                        listOf(
                            ExerciseAttemptRequest(
                                answer = MCQAnswer(interaction.correctValue)
                            )
                        )
                    } else {
                        val wrong = interaction.items.first { it != interaction.correctValue }

                        listOf(
                            ExerciseAttemptRequest(
                                answer = MCQAnswer(wrong)
                            ),
                            ExerciseAttemptRequest(
                                answer = MCQAnswer(interaction.correctValue)
                            )
                        )
                    }
                }

                is ClozeInteraction -> {

                    val correctValues =
                        interaction.blanks.map { it.correctOptions.first() }

                    if (allCorrect) {
                        listOf(
                            ExerciseAttemptRequest(
                                answer = ClozeAnswer(correctValues)
                            )
                        )
                    } else {

                        val wrongValues =
                            interaction.blanks.map { blank ->
                                interaction.options.first { it !in blank.correctOptions }
                            }

                        listOf(
                            ExerciseAttemptRequest(
                                answer = ClozeAnswer(wrongValues)
                            ),
                            ExerciseAttemptRequest(
                                answer = ClozeAnswer(correctValues)
                            )
                        )
                    }
                }
            }

        return ExerciseSubmissionRequest(
            exerciseId = exercise.exerciseId,
            version = exercise.exerciseVersion,
            attempts = attempts
        )
    }
}