package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.support.TestRestClient
import java.util.UUID

object LessonSubmissionTestUtil {

    /**
     * Utility function to complete a lesson with all exercises correct on first attempt.
     * @param userId The user completing the lesson
     * @param lessonSnap The lesson snapshot to complete
     * @param courseId The course ID
     * @param allCorrect If true, all exercises pass on first attempt. If false, first attempt fails then succeeds.
     * @return The lesson completion packet
     */
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

    /**
     * Creates an exercise submission with correct answers.
     * @param exercise The exercise snapshot
     * @param allCorrect If true, correct on first attempt. If false, wrong then correct.
     * @return Exercise submission request
     */
    private fun createExerciseSubmission(
        exercise: ExerciseSnap,
        allCorrect: Boolean = true
    ): ExerciseSubmissionRequest {
        // Handle INFO exercises specially - they need a marker "I" to be skipped
        val attempts = if (exercise.exerciseType == com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType.INFO) {
            listOf(
                ExerciseAttemptRequest(
                    exerciseId = exercise.id,
                    isCorrect = true,
                    answer = listOf(AttemptToken(UUID.randomUUID(), "I")) // Marker for INFO exercises
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


}