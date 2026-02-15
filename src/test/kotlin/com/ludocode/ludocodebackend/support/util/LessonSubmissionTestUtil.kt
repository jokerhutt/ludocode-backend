package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.support.TestRestClient
import java.util.UUID

object LessonSubmissionTestUtil {

    fun completeLesson(
        userId: UUID,
        lessonSnap: LessonSnap,
        courseId: UUID,
        allCorrect: Boolean = true
    ): LessonCompletionPacket {
        val submissions = lessonSnap.exercises.map { exercise ->
            createExerciseSubmission(exercise, allCorrect)
        }

        val request = LessonSubmissionRequest(
            submissionId = UUID.randomUUID(),
            lessonId = lessonSnap.id,
            courseId = courseId,
            submissions = submissions
        )

        return submitPostForLessonSubmission(userId, request)
    }

    private fun submitPostForLessonSubmission(userId: UUID, submission: LessonSubmissionRequest): LessonCompletionPacket =
        TestRestClient.postOk(ApiPaths.PROGRESS.COMPLETION.BASE, userId, submission, LessonCompletionPacket::class.java)

    private fun createExerciseSubmission(
        exercise: ExerciseSnap,
        allCorrect: Boolean = true
    ): ExerciseSubmissionRequest {
        val attempts = if (exercise.exerciseType == ExerciseType.INFO) {
            listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercise.id,
                    isCorrect = true,
                    answer = listOf(AttemptToken(UUID.randomUUID(), "I"))
                )
            )
        } else if (allCorrect) {
            listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercise.id,
                    isCorrect = true,
                    answer = exercise.correctOptions.map {
                        AttemptToken(it.exerciseOptionId, it.content)
                    }
                )
            )
        } else {
            listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercise.id,
                    isCorrect = false,
                    answer = exercise.distractors.map {
                        AttemptToken(it.exerciseOptionId, it.content)
                    }
                ),
                ExerciseAttemptRequest(
                    exerciseId = exercise.id,
                    isCorrect = true,
                    answer = exercise.correctOptions.map {
                        AttemptToken(it.exerciseOptionId, it.content)
                    }
                )
            )
        }

        return ExerciseSubmissionRequest(
            exerciseId = exercise.id,
            version = 1,
            attempts = attempts
        )
    }

    fun createRandomExerciseSubmission(
        exercise: ExerciseSnap,
        random: java.util.Random
    ): ExerciseSubmissionRequest {
        if (exercise.exerciseType == ExerciseType.INFO) {
            return ExerciseSubmissionRequest(
                exerciseId = exercise.id,
                version = 1,
                attempts = listOf(
                    ExerciseAttemptRequest(
                        exerciseId = exercise.id,
                        isCorrect = true,
                        answer = listOf(AttemptToken(UUID.randomUUID(), "I"))
                    )
                )
            )
        }

        if (exercise.distractors.isEmpty()) {
            return ExerciseSubmissionRequest(
                exerciseId = exercise.id,
                version = 1,
                attempts = listOf(
                    ExerciseAttemptRequest(
                        exerciseId = exercise.id,
                        isCorrect = true,
                        answer = exercise.correctOptions.map { AttemptToken(it.exerciseOptionId, it.content) }
                    )
                )
            )
        }

        val numAttempts = random.nextInt(3) + 1
        val attempts = mutableListOf<ExerciseAttemptRequest>()

        for (i in 0 until numAttempts) {
            val isLastAttempt = i == numAttempts - 1
            val isCorrect = isLastAttempt || random.nextBoolean()

            attempts.add(
                ExerciseAttemptRequest(
                    exerciseId = exercise.id,
                    isCorrect = isCorrect,
                    answer = if (isCorrect) {
                        exercise.correctOptions.map { AttemptToken(it.exerciseOptionId, it.content) }
                    } else {
                        exercise.distractors.map { AttemptToken(it.exerciseOptionId, it.content) }
                    }
                )
            )

            if (isCorrect) break
        }

        return ExerciseSubmissionRequest(
            exerciseId = exercise.id,
            version = 1,
            attempts = attempts
        )
    }

    fun createPerfectExerciseSubmission(exercise: ExerciseSnap): ExerciseSubmissionRequest {
        if (exercise.exerciseType == ExerciseType.INFO) {
            return ExerciseSubmissionRequest(
                exerciseId = exercise.id,
                version = 1,
                attempts = listOf(
                    ExerciseAttemptRequest(
                        exerciseId = exercise.id,
                        isCorrect = true,
                        answer = listOf(AttemptToken(UUID.randomUUID(), "I"))
                    )
                )
            )
        }

        return ExerciseSubmissionRequest(
            exerciseId = exercise.id,
            version = 1,
            attempts = listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercise.id,
                    isCorrect = true,
                    answer = exercise.correctOptions.map { AttemptToken(it.exerciseOptionId, it.content) }
                )
            )
        )
    }

    fun createImperfectExerciseSubmission(
        exercise: ExerciseSnap,
        random: java.util.Random
    ): ExerciseSubmissionRequest {
        if (exercise.exerciseType == ExerciseType.INFO) {
            return ExerciseSubmissionRequest(
                exerciseId = exercise.id,
                version = 1,
                attempts = listOf(
                    ExerciseAttemptRequest(
                        exerciseId = exercise.id,
                        isCorrect = true,
                        answer = listOf(AttemptToken(UUID.randomUUID(), "I"))
                    )
                )
            )
        }

        if (exercise.distractors.isEmpty()) {
            return ExerciseSubmissionRequest(
                exerciseId = exercise.id,
                version = 1,
                attempts = listOf(
                    ExerciseAttemptRequest(
                        exerciseId = exercise.id,
                        isCorrect = true,
                        answer = exercise.correctOptions.map { AttemptToken(it.exerciseOptionId, it.content) }
                    )
                )
            )
        }

        val numWrongAttempts = random.nextInt(2) + 1
        val attempts = mutableListOf<ExerciseAttemptRequest>()

        repeat(numWrongAttempts) {
            attempts.add(
                ExerciseAttemptRequest(
                    exerciseId = exercise.id,
                    isCorrect = false,
                    answer = exercise.distractors.map { AttemptToken(it.exerciseOptionId, it.content) }
                )
            )
        }

        attempts.add(
            ExerciseAttemptRequest(
                exerciseId = exercise.id,
                isCorrect = true,
                answer = exercise.correctOptions.map { AttemptToken(it.exerciseOptionId, it.content) }
            )
        )

        return ExerciseSubmissionRequest(
            exerciseId = exercise.id,
            version = 1,
            attempts = attempts
        )
    }

}